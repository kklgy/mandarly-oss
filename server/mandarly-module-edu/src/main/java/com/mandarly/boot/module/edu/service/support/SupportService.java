package com.mandarly.boot.module.edu.service.support;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryStatsRespVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportTopUnmatchedRespVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskReqVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskRespVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportBootstrapRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.FaqDO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportContactDO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportInquiryLogDO;

import java.util.List;

public interface SupportService {

    AppSupportBootstrapRespVO bootstrap(String locale, String market);

    AppSupportAskRespVO ask(AppSupportAskReqVO reqVO, Long userId, String ip, String userAgent);

    /**
     * Phase 1 兼容双路径:
     * - logId 非空 → 更新已有 inquiry_log 的 clicked_to_human / clicked_contact_id(二期 chat UI 走这条)
     * - logId 为空 → 新建一行"纯渠道点击"日志(Phase 1 SupportChatWidget 直接点联系方式走这条),
     *   question_text 留空,clicked_to_human=true。
     */
    void markContactClicked(Long logId, Long contactId, String sessionId, String locale,
                            String market, Long userId, String ip, String userAgent);

    PageResult<FaqDO> getFaqPage(SupportFaqPageReqVO reqVO);

    FaqDO getFaq(Long id);

    Long createFaq(SupportFaqSaveReqVO reqVO);

    void updateFaq(SupportFaqSaveReqVO reqVO);

    void deleteFaq(Long id);

    PageResult<SupportContactDO> getContactPage(SupportContactPageReqVO reqVO);

    SupportContactDO getContact(Long id);

    Long createContact(SupportContactSaveReqVO reqVO);

    void updateContact(SupportContactSaveReqVO reqVO);

    void deleteContact(Long id);

    PageResult<SupportInquiryLogDO> getInquiryPage(SupportInquiryPageReqVO reqVO);

    SupportInquiryStatsRespVO getInquiryStats();

    List<SupportTopUnmatchedRespVO> getTopUnmatched(Integer limit);
}
