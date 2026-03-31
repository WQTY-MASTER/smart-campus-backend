# -*- coding: utf-8 -*-
"""生成最小合法 .docx（仅标准库）。运行: python build_report_docx.py"""
import os
import zipfile
import xml.sax.saxutils as xu

OUT = os.path.join(os.path.dirname(__file__), "课程设计进度汇报-智享校园抢课系统.docx")

CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

DOC_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>
"""


def esc(s):
    return xu.escape(s)


def p(text, bold=False):
    t = esc(text)
    if bold:
        return (
            "<w:p><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr>"
            "<w:r><w:rPr><w:b/></w:rPr><w:t xml:space=\"preserve\">%s</w:t></w:r></w:p>" % t
        )
    return "<w:p><w:r><w:t xml:space=\"preserve\">%s</w:t></w:r></w:p>" % t


def heading1(text):
    return (
        "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>"
        "<w:r><w:rPr><w:b/><w:sz w:val=\"32\"/></w:rPr>"
        "<w:t xml:space=\"preserve\">%s</w:t></w:r></w:p>" % esc(text)
    )


def heading2(text):
    return (
        "<w:p><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr>"
        "<w:r><w:rPr><w:b/><w:sz w:val=\"28\"/></w:rPr>"
        "<w:t xml:space=\"preserve\">%s</w:t></w:r></w:p>" % esc(text)
    )


def table(rows):
    """rows: list of list of str"""
    tbl = [
        "<w:tbl>",
        "<w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/><w:tblBorders>"
        "<w:top w:val=\"single\" w:sz=\"4\"/><w:left w:val=\"single\" w:sz=\"4\"/>"
        "<w:bottom w:val=\"single\" w:sz=\"4\"/><w:right w:val=\"single\" w:sz=\"4\"/>"
        "<w:insideH w:val=\"single\" w:sz=\"4\"/><w:insideV w:val=\"single\" w:sz=\"4\"/>"
        "</w:tblBorders></w:tblPr>",
    ]
    for i, row in enumerate(rows):
        tbl.append("<w:tr>")
        for cell in row:
            tc = (
                "<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>"
                "<w:p><w:r><w:t xml:space=\"preserve\">%s</w:t></w:r></w:p></w:tc>"
            ) % esc(cell)
            tbl.append(tc)
        tbl.append("</w:tr>")
    tbl.append("</w:tbl>")
    return "".join(tbl)


def build_document_xml():
    parts = []
    parts.append(
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
        "<w:body>"
    )
    parts.append(
        p("基于 Java 的 Web 高并发应用程序——课程设计进度汇报（修订稿）", bold=True)
    )
    parts.append(heading1("一、所选题目"))
    parts.append(
        p(
            "本次课程设计选题为基于 Java 的 Web 高并发应用，项目名称为智享校园 —— 热门公选课抢课系统，"
            "面向校园场景中「大量学生同时争抢有限热门选修课名额」的典型高并发问题，"
            "重点验证读性能与写一致性（不超卖、防重复选课）。"
        )
    )
    parts.append(heading1("二、开发工具"))
    parts.append(
        table(
            [
                ["类别", "具体工具 / 技术", "用途说明"],
                [
                    "后端",
                    "IntelliJ IDEA、JDK 8（pom）、Spring Boot 2.6",
                    "REST 接口、JPA（Hibernate）访问 PostgreSQL",
                ],
                ["前端", "Node.js、Vue 2 + Element UI", "学生端与管理端界面"],
                ["数据库", "PostgreSQL", "课程、学生、选课、日志、配置等持久化"],
                ["缓存", "Redis", "课程缓存，减轻高并发读压力"],
                ["测试", "JMeter、Postman", "压测与接口调试"],
            ]
        )
    )
    parts.append(
        p(
            "说明：若课程要求写 JDK 17，以本机实际编译运行版本为准，并与 pom 中 java.version 保持一致说明。"
        )
    )
    parts.append(heading1("三、设计思路"))
    parts.append(heading2("1. 核心目标"))
    parts.append(
        p(
            "读得快：课程列表等高频查询尽量走 Redis；抢得准：库存采用数据库原子条件更新（left_stock>0 再减 1）；"
            "防重抢：学生+课程唯一或存在性校验；可运营：抢课时间窗/开关/退课截止、Excel 导出。"
        )
    )
    parts.append(heading2("2. 功能模块"))
    parts.append(
        p(
            "学生端：学号密码登录；课程列表（可结合 Redis）；抢课（时间窗+开关+扣库存+日志）；"
            "我的课表与退课（恢复库存、配置约束、更新缓存）。"
        )
    )
    parts.append(
        p(
            "管理端：管理员登录；课程 CRUD 与多条件分页；system_config 抢课配置；选课统计与 EasyExcel 导出。"
        )
    )
    parts.append(heading2("3. 高并发要点"))
    parts.append(
        table(
            [
                ["方案", "解决问题", "范围"],
                ["Redis 缓存", "热点读放大", "读多写少"],
                ["条件更新扣库存", "不超卖", "抢课写路径"],
                ["唯一/存在性校验", "防重复选课", "选课写入"],
                ["抢课日志", "可追溯", "旁路记录"],
            ]
        )
    )
    parts.append(heading1("四、进展情况（结题版）"))
    parts.append(heading2("1. 已完成"))
    parts.append(
        p(
            "Spring Boot + PostgreSQL + Redis + JPA 工程；course、student、student_course、admin、"
            "system_config、seckill_log 等表与全流程业务；Redis 缓存与自定义 Repository 解决分页 SQL 绑定问题；"
            "可按需关闭 show-sql。"
        )
    )
    parts.append(heading2("2. 可选加强"))
    parts.append(
        p(
            "JWT 拦截器、JMeter 压测报告与数据表、Redis 降级与限流等可作为扩展。"
        )
    )
    parts.append(heading2("3. 中期问题回顾"))
    parts.append(
        p(
            "Redis 未启动、Hibernate 原生 SQL 参数绑定等问题已在本阶段解决。"
        )
    )
    parts.append(heading1("五、小结"))
    parts.append(
        p(
            "已完成登录、抢课、退课、教务管理与导出完整链路，体现 Redis + 数据库一致性 + 业务防重的高并发设计目标。"
        )
    )
    parts.append("</w:body></w:document>")
    return "".join(parts)


def main():
    xml = build_document_xml()
    with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", CONTENT_TYPES)
        z.writestr("_rels/.rels", RELS)
        z.writestr("word/_rels/document.xml.rels", DOC_RELS)
        z.writestr("word/document.xml", xml)
    print("已生成:", OUT)


if __name__ == "__main__":
    main()
