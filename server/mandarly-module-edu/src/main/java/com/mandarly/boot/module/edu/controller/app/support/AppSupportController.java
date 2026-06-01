package com.mandarly.boot.module.edu.controller.app.support;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskReqVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskRespVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportBootstrapRespVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportContactClickReqVO;
import com.mandarly.boot.module.edu.service.support.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "用户端 - 客服 FAQ 机器人(公开)")
@RestController
@RequestMapping("/edu/support")
@Validated
public class AppSupportController {

    @Resource
    private SupportService supportService;

    @GetMapping("/bootstrap")
    @PermitAll
    @Operation(summary = "初始化客服窗口,返回开场白与真人客服联系方式")
    public CommonResult<AppSupportBootstrapRespVO> bootstrap(@RequestParam(value = "locale", required = false) String locale,
                                                             @RequestParam(value = "market", required = false) String market) {
        return success(supportService.bootstrap(locale, market));
    }

    @PostMapping("/ask")
    @PermitAll
    @Operation(summary = "提交客服问题,返回 FAQ 命中结果或真人客服引流")
    public CommonResult<AppSupportAskRespVO> ask(@Valid @RequestBody AppSupportAskReqVO reqVO,
                                                 HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return success(supportService.ask(reqVO, userId, ip, userAgent));
    }

    @PostMapping("/contact-click")
    @PermitAll
    @Operation(summary = "记录用户点击真人客服")
    public CommonResult<Boolean> contactClick(@Valid @RequestBody AppSupportContactClickReqVO reqVO,
                                              HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        supportService.markContactClicked(reqVO.getLogId(), reqVO.getContactId(),
                reqVO.getSessionId(), reqVO.getLocale(), reqVO.getMarket(),
                userId, ip, userAgent);
        return success(true);
    }
}
