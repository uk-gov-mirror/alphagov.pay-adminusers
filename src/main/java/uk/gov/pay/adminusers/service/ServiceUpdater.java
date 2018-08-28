package uk.gov.pay.adminusers.service;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.adminusers.exception.ServiceNotFoundException;
import uk.gov.pay.adminusers.model.Service;
import uk.gov.pay.adminusers.model.ServiceUpdateRequest;
import uk.gov.pay.adminusers.model.UpdateMerchantDetailsRequest;
import uk.gov.pay.adminusers.persistence.dao.ServiceDao;
import uk.gov.pay.adminusers.persistence.entity.MerchantDetailsEntity;
import uk.gov.pay.adminusers.persistence.entity.ServiceEntity;
import uk.gov.pay.adminusers.persistence.entity.service.ServiceNameEntity;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static uk.gov.pay.adminusers.service.AdminUsersExceptions.conflictingServiceGatewayAccounts;

public class ServiceUpdater {
 
    public static final String FIELD_NAME = "name";
    public static final String FIELD_GATEWAY_ACCOUNT_IDS = "gateway_account_ids";
    public static final String FIELD_CUSTOM_BRANDING = "custom_branding";
    public static final String FIELD_SERVICE_NAME_PREFIX = "service_name";

    private final ServiceDao serviceDao;

    private final Map<String, BiConsumer<ServiceUpdateRequest, ServiceEntity>> attributeUpdaters;

    @Inject
    public ServiceUpdater(ServiceDao serviceDao) {
        ImmutableMap.Builder<String, BiConsumer<ServiceUpdateRequest, ServiceEntity>> attributeUpdaters = ImmutableMap.builder();
        attributeUpdaters.put(FIELD_NAME, updateServiceName());
        attributeUpdaters.put(FIELD_GATEWAY_ACCOUNT_IDS, assignGatewayAccounts());
        attributeUpdaters.put(FIELD_CUSTOM_BRANDING, updateCustomBranding());
        Arrays.stream(SupportedLanguage.values())
                .forEach(language -> attributeUpdaters.put(FIELD_SERVICE_NAME_PREFIX + '/' + language.toString(), updateMultilingualServiceName()));
        this.attributeUpdaters = attributeUpdaters.build();
        this.serviceDao = serviceDao;
    }

    @Transactional
    public Optional<Service> doUpdate(String serviceExternalId, ServiceUpdateRequest updateRequests) {
        return doUpdate(serviceExternalId, Collections.singletonList(updateRequests));
    }

    @Transactional
    public Optional<Service> doUpdate(String serviceExternalId, List<ServiceUpdateRequest> updateRequests) {
        return serviceDao.findByExternalId(serviceExternalId)
                .flatMap(serviceEntity -> {
                    updateRequests.forEach(req -> {
                        attributeUpdaters.get(req.getPath())
                                .accept(req, serviceEntity);
                        serviceDao.merge(serviceEntity);
                    });
                    return Optional.of(serviceEntity.toService());
                });
    }

    @Transactional
    public Service doUpdateMerchantDetails(String serviceExternalId, UpdateMerchantDetailsRequest updateMerchantDetailsRequest) throws ServiceNotFoundException {
        return serviceDao.findByExternalId(serviceExternalId)
                .map(serviceEntity -> {
                    MerchantDetailsEntity merchantEntity = MerchantDetailsEntity.from(updateMerchantDetailsRequest);
                    serviceEntity.setMerchantDetailsEntity(merchantEntity);
                    serviceDao.merge(serviceEntity);
                    return serviceEntity.toService();
                }).orElseThrow(() -> new ServiceNotFoundException(serviceExternalId));
    }

    private BiConsumer<ServiceUpdateRequest, ServiceEntity> updateServiceName() {
        return (serviceUpdateRequest, serviceEntity) -> serviceEntity.setName(serviceUpdateRequest.valueAsString());
    }

    private BiConsumer<ServiceUpdateRequest, ServiceEntity> assignGatewayAccounts() {
        return (serviceUpdateRequest, serviceEntity) -> {
            List<String> gatewayAccountIds = serviceUpdateRequest.valueAsList();
            if (serviceDao.checkIfGatewayAccountsUsed(gatewayAccountIds)) {
                throw conflictingServiceGatewayAccounts(gatewayAccountIds);
            } else {
                serviceEntity.addGatewayAccountIds(gatewayAccountIds.toArray(new String[0]));
            }
        };
    }

    private BiConsumer<ServiceUpdateRequest, ServiceEntity> updateCustomBranding() {
        return (serviceUpdateRequest, serviceEntity) -> serviceEntity.setCustomBranding(serviceUpdateRequest.valueAsObject());
    }

    private BiConsumer<ServiceUpdateRequest, ServiceEntity> updateMultilingualServiceName() {
        return (serviceUpdateRequest, serviceEntity) -> {
            String path = serviceUpdateRequest.getPath();
            assert path.matches(FIELD_SERVICE_NAME_PREFIX + "/[a-z]+") : "Path must be 'service_name/en' etc.";
            SupportedLanguage language = SupportedLanguage.fromIso639AlphaTwoCode(serviceUpdateRequest.getPath().substring(path.indexOf('/') + 1));
            ServiceNameEntity serviceNameEntity = ServiceNameEntity.from(language, serviceUpdateRequest.valueAsString());
            serviceEntity.addOrUpdateServiceName(serviceNameEntity);
        };
    }
}
