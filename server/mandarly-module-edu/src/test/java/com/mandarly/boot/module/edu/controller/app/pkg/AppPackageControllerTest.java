package com.mandarly.boot.module.edu.controller.app.pkg;

import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.pkg.vo.AppPackageRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.service.i18n.I18nMessageService;
import com.mandarly.boot.module.edu.service.pkg.PackageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppPackageControllerTest {

    @Mock
    private PackageService packageService;

    @Mock
    private I18nMessageService i18nMessageService;

    @InjectMocks
    private AppPackageController controller;

    private MockedStatic<SecurityFrameworkUtils> securityStatic;

    @AfterEach
    void teardown() {
        if (securityStatic != null) {
            securityStatic.close();
        }
    }

    @Test
    void list_returnsMaskedPricesForAnonymousVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(null);
        when(packageService.getPublicPackageList("HKD")).thenReturn(List.of(packageRow()));
        when(i18nMessageService.translateMap(anySet(), eq("zh-CN")))
                .thenReturn(Map.of(
                        "package.name.half_year_1pw", "半年包",
                        "packages.discountLabel.year10off", "9 折"
                ));

        AppPackageRespVO vo = controller.list("HKD", "zh-CN").getData().get(0);

        assertFalse(vo.getPriceVisible());
        assertNull(vo.getPrice());
        assertNull(vo.getCurrency());
        assertEquals("半年包", vo.getName());
        assertNull(vo.getDiscountLabel());
    }

    @Test
    void list_returnsPriceFieldsForLoggedInVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(1001L);
        when(packageService.getPublicPackageList("HKD")).thenReturn(List.of(packageRow()));
        when(i18nMessageService.translateMap(anySet(), eq("zh-CN")))
                .thenReturn(Map.of(
                        "package.name.half_year_1pw", "半年包",
                        "packages.discountLabel.year10off", "9 折"
                ));

        AppPackageRespVO vo = controller.list("HKD", "zh-CN").getData().get(0);

        assertTrue(vo.getPriceVisible());
        assertEquals(new BigDecimal("6000.00"), vo.getPrice());
        assertEquals("HKD", vo.getCurrency());
        assertEquals(new BigDecimal("230.77"), vo.getPricePerSession());
        assertEquals("9 折", vo.getDiscountLabel());
    }

    private PackageDO packageRow() {
        PackageDO p = new PackageDO();
        p.setId(1L);
        p.setNameI18nCode("package.name.half_year_1pw");
        p.setWeeklyCount(1);
        p.setTotalCount(26);
        p.setValidityDays(180);
        p.setPrice(new BigDecimal("6000.00"));
        p.setCurrency("HKD");
        p.setIsFreeTrial(false);
        p.setIsActive(true);
        p.setShowOnListPriority(1);
        p.setDiscountLabel("packages.discountLabel.year10off");
        return p;
    }
}
