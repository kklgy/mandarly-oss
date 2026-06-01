package com.mandarly.boot.module.edu.controller.app.sitemap;

import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.service.teacher.TeacherProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Mandarly 公开站点的 sitemap.xml 动态生成。
 *
 * 路径:
 *   后端原始: /app-api/edu/sitemap.xml(yudao 自动给 controller.app.* 包加 /app-api 前缀)
 *   对外可见: http://localhost:3001/sitemap.xml(由 nginx `location = /sitemap.xml` 反代)
 *
 * 内容:
 *   - 4 个静态公开页:/ /teachers /level-check(每页一条 url)
 *   - 动态:listVisibleTeachers() 返回的每个教师 → /teacher/{userId}
 *   - 多语言 hreflang:一期 SPA 同 URL,zh-CN/zh-TW/en/ar + x-default 都指回同一 loc
 *
 * 详见 docs/frontend/seo-geo-checklist.md §1.7。
 */
@Tag(name = "用户端 - 公共 - sitemap")
@RestController
@PermitAll
public class PublicSitemapController {

    private static final String SITE_ORIGIN = "http://localhost:3001";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Resource
    private TeacherProfileService teacherProfileService;

    @GetMapping(value = "/edu/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "公开 sitemap.xml(静态页 + 动态教师列表)")
    public ResponseEntity<String> sitemap() {
        String today = LocalDate.now().format(DATE_FMT);
        StringBuilder xml = new StringBuilder(2048);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"")
           .append(" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">\n");

        appendStaticUrl(xml, "/", today, "weekly", "1.0", true);
        appendStaticUrl(xml, "/teachers", today, "daily", "0.9", false);
        appendStaticUrl(xml, "/level-check", today, "monthly", "0.8", false);

        List<TeacherProfileDO> teachers = teacherProfileService.listVisibleTeachers();
        for (TeacherProfileDO t : teachers) {
            String loc = SITE_ORIGIN + "/teacher/" + t.getUserId();
            xml.append("  <url>\n")
               .append("    <loc>").append(escape(loc)).append("</loc>\n")
               .append("    <lastmod>").append(today).append("</lastmod>\n")
               .append("    <changefreq>weekly</changefreq>\n")
               .append("    <priority>0.7</priority>\n")
               .append("  </url>\n");
        }

        xml.append("</urlset>\n");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.toString());
    }

    private void appendStaticUrl(StringBuilder xml, String path, String lastmod,
                                 String changefreq, String priority, boolean withHreflang) {
        String loc = SITE_ORIGIN + path;
        xml.append("  <url>\n")
           .append("    <loc>").append(escape(loc)).append("</loc>\n")
           .append("    <lastmod>").append(lastmod).append("</lastmod>\n")
           .append("    <changefreq>").append(changefreq).append("</changefreq>\n")
           .append("    <priority>").append(priority).append("</priority>\n");
        if (withHreflang) {
            for (String lang : new String[] {"zh-CN", "zh-TW", "en", "ar", "x-default"}) {
                xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"").append(lang)
                   .append("\" href=\"").append(escape(loc)).append("\" />\n");
            }
        }
        xml.append("  </url>\n");
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
