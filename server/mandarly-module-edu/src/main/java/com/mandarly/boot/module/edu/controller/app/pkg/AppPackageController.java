package com.mandarly.boot.module.edu.controller.app.pkg;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.pkg.vo.AppPackageRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.service.i18n.I18nMessageService;
import com.mandarly.boot.module.edu.service.pkg.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "用户端 - 套餐列表")
@RestController
@RequestMapping("/edu/package")
public class AppPackageController {

    @Resource
    private PackageService packageService;

    @Resource
    private I18nMessageService i18nMessageService;

    @GetMapping("/list")
    @Operation(summary = "公开套餐列表(is_active=1 且非 free_trial),Wave 4 加 currency 参数 + i18n 翻译")
    @PermitAll
    public CommonResult<List<AppPackageRespVO>> list(
            @Parameter(description = "货币 HKD/USD/CNY,可选,默认全部")
            @RequestParam(value = "currency", required = false) String currency,
            @Parameter(description = "locale, 用于翻译 recommendationLabel/discountLabel/sellingPoints i18n code")
            @RequestParam(value = "locale", required = false) String locale) {
        // D27-C:解除 "未登录返空" 一刀切,改为返完整列表 + price 字段按 isLoggedIn 脱敏
        // 让匿名用户在 home / packages 页能看到套餐卡(锁定毛玻璃态),登录后看真实价格
        boolean priceVisible = SecurityFrameworkUtils.getLoginUserId() != null;

        List<PackageDO> rows = packageService.getPublicPackageList(currency);
        if (rows == null || rows.isEmpty()) {
            return success(Collections.emptyList());
        }

        // 批量收集 i18n_message code,降低 N+1
        Set<String> codes = new HashSet<>();
        for (PackageDO p : rows) {
            if (p.getNameI18nCode() != null && !p.getNameI18nCode().isBlank()) {
                codes.add(p.getNameI18nCode());
            }
            if (p.getRecommendationLabel() != null && !p.getRecommendationLabel().isBlank()) {
                codes.add(p.getRecommendationLabel());
            }
            if (p.getDiscountLabel() != null && !p.getDiscountLabel().isBlank()) {
                codes.add(p.getDiscountLabel());
            }
            if (p.getSellingPoints() != null) {
                for (String c : p.getSellingPoints()) {
                    if (c != null && !c.isBlank()) {
                        codes.add(c);
                    }
                }
            }
        }
        Map<String, String> tx = codes.isEmpty()
                ? Collections.emptyMap()
                : i18nMessageService.translateMap(codes, locale);

        List<AppPackageRespVO> list = new ArrayList<>(rows.size());
        for (PackageDO p : rows) {
            AppPackageRespVO vo = new AppPackageRespVO();
            vo.setId(p.getId());
            vo.setNameI18nCode(p.getNameI18nCode());
            vo.setName(translateOrCode(tx, p.getNameI18nCode()));
            vo.setDescriptionI18nCode(p.getDescriptionI18nCode());
            vo.setWeeklyCount(p.getWeeklyCount());
            vo.setTotalCount(p.getTotalCount());
            vo.setValidityDays(p.getValidityDays());
            vo.setPriceVisible(priceVisible);
            if (priceVisible) {
                vo.setPrice(p.getPrice());
                vo.setCurrency(p.getCurrency());
            }
            vo.setIsFreeTrial(p.getIsFreeTrial());
            vo.setIsActive(p.getIsActive());
            vo.setShowOnListPriority(p.getShowOnListPriority());

            // i18n 翻译: 找不到 code 时返回原 code(或 null), 前端按非空判断显隐
            vo.setRecommendationLabel(translateOrCode(tx, p.getRecommendationLabel()));
            if (priceVisible) {
                vo.setDiscountLabel(translateOrCode(tx, p.getDiscountLabel()));
            }

            if (p.getSellingPoints() != null && !p.getSellingPoints().isEmpty()) {
                List<String> points = new ArrayList<>(p.getSellingPoints().size());
                for (String code : p.getSellingPoints()) {
                    String txt = translateOrCode(tx, code);
                    if (txt != null && !txt.isBlank()) {
                        points.add(txt);
                    }
                }
                vo.setSellingPoints(points);
            }

            // 单节均价: price / totalCount, 后端预算好避免前端浮点误差
            if (priceVisible && p.getPrice() != null && p.getTotalCount() != null && p.getTotalCount() > 0) {
                vo.setPricePerSession(p.getPrice()
                        .divide(BigDecimal.valueOf(p.getTotalCount()), 2, RoundingMode.HALF_UP));
            }

            list.add(vo);
        }
        return success(list);
    }

    private String translateOrCode(Map<String, String> tx, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String t = tx.get(code);
        return (t == null || t.isBlank()) ? code : t;
    }
}
