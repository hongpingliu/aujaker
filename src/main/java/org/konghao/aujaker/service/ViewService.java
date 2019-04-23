package org.konghao.aujaker.service;

import org.konghao.aujaker.kit.CommonKit;
import org.konghao.aujaker.model.ClassEntity;
import org.konghao.aujaker.model.FinalValue;
import org.konghao.aujaker.model.PropertiesBaseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Created by 钟述林 393156105@qq.com on 2017/5/3 14:58.
 */
@Service
public class ViewService implements IViewService {
    @Override
    public void generateViews(String path, Map<String, Object> maps) {
        String artifactId = (String)maps.get(FinalValue.ARTIFACT_ID);
        String groupId = (String)maps.get(FinalValue.GROUP_ID);
        List<ClassEntity> entitys = (List<ClassEntity>)maps.get(FinalValue.ENTITY);

        generateMenu(path, artifactId, entitys);
        generateIndexController(path, entitys.get(0), artifactId, groupId);

        for(ClassEntity ce:entitys) {
            generateView(path,ce,artifactId);
            generateModify(path, artifactId, "add", ce);
            generateModify(path, artifactId, "update", ce);
            generateList(path, artifactId, ce);
        }

        generateIndexPage(path, entitys, artifactId);
    }

    private void generateIndexPage(String path, List<ClassEntity> entities, String artifactId) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(generatePath(path, artifactId, "")+"/index.html"), true, "UTF-8");
            ps.println("<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\"\n" +
                    "      xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "    <head>\n" +
                    "        <title>系统首页</title>\n" +
                    "\n" +
                    "        <meta charset=\"utf-8\"/>\n" +
                    "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>\n" +
                    "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n" +
                    "\n" +
                    "        <script type=\"text/javascript\" src=\"/basic/js-lib/jquery-1.12.3.min.js\"></script>\n" +
                    "        <script type=\"text/javascript\" src=\"/basic/bootstrap3/js/bootstrap.min.js\"></script>\n" +
                    "        <link type=\"text/css\" rel=\"stylesheet\" href=\"/basic/bootstrap3/css/bootstrap.min.css\"/>\n" +
                    "\n" +
                    "        <!-- Font Awesome-->\n" +
                    "        <link rel=\"stylesheet\" href=\"/basic/font-awesome-4.7.0/css/font-awesome.min.css\" />\n" +
                    "        <script type=\"text/javascript\" src=\"/basic/validate/bootstrapValidator.js\"></script>\n" +
                    "        <style>\n" +
                    "            body, html {\n" +
                    "                background:#f9f9f9;\n" +
                    "            }\n" +
                    "        </style>\n" +
                    "    </head>\n" +
                    "    <body>\n\t\t<div class=\"container\" style=\"background: #FFF\">\n");
            ps.println("\t\t<div class=\"page-header\">\n" +
                    "            <h1>默认首页 <small>自动生成</small></h1>\n" +
                    "        </div>\n");

            ps.println("\t\t<div class=\"panel panel-info\">\n" +
                    "            <div class=\"panel-heading\">主要功能展示</div>\n" +
                    "            <div class=\"panel-body\">\n");

            for(ClassEntity entity : entities) {
                ps.println("\t\t\t\t<a href=\"/"+CommonKit.lowcaseFirst(entity.getClassName())+"/list\" class=\"btn btn-lg btn-primary\"><i class=\"fa fa-angle-right\"></i> "+entity.getClassShowName()+"管理</a>");
            }

            ps.println("\t\t\t</div>\n\t\t</div>\n");
            ps.println("\t\t</div>\n\t</body>\n</html>");
        } catch (Exception e) {
        } finally {
            if(ps!=null) {ps.close();}
        }
    }

    private void generateIndexController(String path, ClassEntity entity, String artifactId, String groupId) {
        path = CommonKit.generatePath(path, artifactId, entity, "controller");
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(path+"/IndexController"+".java"), true, "UTF-8");
            //输出包名
            ps.println("package "+groupId+".controller;\n\n");
            ps.println("import org.springframework.stereotype.Controller;\n" +
                    "import org.springframework.web.bind.annotation.GetMapping;\n" +
                    "\n" +
                    "@Controller\n" +
                    "public class IndexController {\n" +
                    "\n" +
                    "    @GetMapping({\"\", \"/\", \"/index\"})\n" +
                    "    public String index() {\n" +
                    "        return \"index\";\n" +
                    "    }\n" +
                    "}\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(ps!=null) ps.close();
        }
    }

    private void generateView(String path, ClassEntity entity, String artifactId) {
        path = generatePath(path, artifactId, entity);
       generateNav(path, entity);
    }

    private void generateList(String path, String artifactId, ClassEntity entity) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(generatePath(path, artifactId, entity) + "/list.html"), true, "UTF-8");
            ps.println("<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\"\n" +
                    "\t  xmlns=\"http://www.w3.org/1999/xhtml\"\n" +
                    "\t  xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "\t  xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "\t  layout:decorator=\"fragments/adminModel\">\n" +
                    "\t<head>\n" +
                    "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                    "\t\t<title>"+entity.getClassShowName()+"列表</title>\n" +
                    "\t\t<script type=\"text/javascript\">\n" +
                    "\t\t\t$(function() {\n" +
                    "\t\t\t\t$(\".delete-obj-href\").deleteFun();\n" +
                    "\t\t\t});\n" +
                    "\t\t</script>\n" +
                    "\t</head>\n" +
                    "\t<body>\n" +
                    "\t\t<div th:fragment=\"content\" th:remove=\"tag\">\n");

            ps.println("\t\t<div class=\"header lighter smaller blue\">\n" +
                    "\t\t\t<h3><span class=\"glyphicon glyphicon-th-list\"></span>&nbsp;"+entity.getClassShowName()+"列表（<span th:text=\"${datas.getTotalElements()}\"></span>）</h3>\n" +
                    "\t\t\t<div th:replace=\""+CommonKit.lowcaseFirst(entity.getClassName())+"/nav :: content\" th:remove=\"tag\"></div>\n" +
                    "\t\t</div>\n");

            ps.println("\t\t<div class=\"table-responsive\">\n");

            ps.println("\t\t\t<table class=\"table table-hover\">");

            ps.println("\t\t\t\t<thead>\n" +
                    "\t\t\t\t\t<tr>\n");
            for(PropertiesBaseEntity pbe : entity.getProps()) {
                ps.println("\t\t\t\t\t\t<th>"+pbe.getCommet()+"</th>\n");
            }

            ps.println("\t\t\t\t\t\t<th>操作</th>\n");
            ps.println("\t\t\t\t\t</tr>\n" +
                    "\t\t\t\t</thead>");

            ps.println("\t\t\t\t<tr th:each=\"obj : ${datas}\">\n");

            for(PropertiesBaseEntity pbe : entity.getProps()) {
                ps.println("\t\t\t\t\t<td th:text=\"${obj."+pbe.getName()+"}\"></td>\n");
            }

            ps.println("\t\t\t\t\t<td>\n" +
                    "\t\t\t\t\t\t<div class=\"action-buttons\">\n" +
                    "\t\t\t\t\t\t\t<a class=\"green auth1\" sn=\""+entity.getClassName()+"Controller.update\" title=\"修改\" th:href=\"'/"+CommonKit.lowcaseFirst(entity.getClassName())+"/update/'+${obj.id}\">\n" +
                    "\t\t\t\t\t\t\t\t<i class=\"fa fa-pencil\"></i>\n" +
                    "\t\t\t\t\t\t\t</a>\n" +
                    "\n" +
                    "\t\t\t\t\t\t\t<a class=\"delete-obj-href red auth1\" sn=\""+entity.getClassName()+"Controller.delete\" th:title=\"'此操作不可逆，确定删除吗？'\" th:href=\"'/"+CommonKit.lowcaseFirst(entity.getClassName())+"/delete/'+${obj.id}\">\n" +
                    "\t\t\t\t\t\t\t\t<i class=\"fa fa-remove\"></i>\n" +
                    "\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t</td>");

            ps.println("\t\t\t\t</tr>\n");

            ps.println("\t\t\t</table>");

            ps.println("\t\t\t<div th:include=\"fragments/page :: pager\" th:remove=\"tag\"></div>\n" +
                    "\t\t</div>");

            ps.println("\t\t</div>\n\t</body>\n" +
                    "</html>");
        } catch(Exception e) {
        } finally {
            if(ps!=null) {ps.close();}
        }
    }

    private void generateModify(String path, String artifactId, String fun, ClassEntity entity) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(generatePath(path, artifactId, entity)+"/"+fun+".html"), true, "UTF-8");
            ps.println("<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\"\n" +
                    "\t  xmlns=\"http://www.w3.org/1999/xhtml\"\n" +
                    "\t  xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "\t  xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "\t  layout:decorator=\"fragments/adminModel\">\n" +
                    "\t<head>\n" +
                    "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                    "\t\t<title>更新"+entity.getClassShowName()+"</title>\n" +
                    "\t</head>\n"+
                    "\t<body>\n\t\t<div th:fragment=\"content\" th:remove=\"tag\">\n");

            ps.println("\t\t\t<div class=\"header lighter smaller blue\">\n" +
                    "\t\t\t\t<h3><i class=\"fa fa-plus\"></i>&nbsp;更新"+entity.getClassShowName()+"</h3>\n" +
                    "\t\t\t\t<div th:replace=\""+CommonKit.lowcaseFirst(entity.getClassName())+"/nav :: content\" th:remove=\"tag\"></div>\n" +
                    "\t\t\t</div>\n");

            ps.println("\t\t\t<form method=\"POST\" th:object=\"${"+CommonKit.lowcaseFirst(entity.getClassName())+"}\" id=\"dataForm\">\n");

            for(PropertiesBaseEntity pbe : entity.getProps()) {
                if(!"id".equalsIgnoreCase(pbe.getName()) && !"java.util.Date".equalsIgnoreCase(pbe.getType())) {
                    ps.println("\t\t\t\t<div class=\"form-group form-group-lg\">\n" +
                            "\t\t\t\t\t<div class=\"input-group\">\n" +
                            "\t\t\t\t\t\t<div class=\"input-group-addon\">" + pbe.getCommet() + "：</div>\n" +
                            "\t\t\t\t\t\t<input name=\"" + pbe.getName() + "\" type=\"text\" class=\"form-control\" th:value=\"${" + CommonKit.lowcaseFirst(entity.getClassName()) + "." + pbe.getName() + "}\" placeholder=\"请输入" + pbe.getCommet() + "\" />\n" +
                            "\t\t\t\t\t</div>\n" +
                            "\t\t\t\t</div>\n");
                }
            }

            ps.println("\t\t\t\t<input type=\"hidden\" name=\"token\" value=\"${session.token}\"/>\n" +
                    "\t\t\t\t<button type=\"submit\" class=\"btn btn-primary\">确定提交</button>");

            ps.println("\t\t\t</form>\n");

            ps.println("\t\t</div>\n\t</body>\n" +
                    "</html>");
        } catch (Exception e) {
        } finally {
            if(ps!=null) {ps.close();}
        }
    }

    private void generateMenu(String path, String artifactId, List<ClassEntity> entities) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(generatePath(path, artifactId, "fragments")+"/nav.html"), true, "UTF-8");
            ps.println("<!DOCTYPE html>\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\"\n" +
                    "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "      layout:decorator=\"fragments/adminModel\">");

            ps.println("<head>\n" +
                    "        <title>导航菜单</title>\n" +
                    "    </head>\n" +
                    "    <body>\n" +
                    "        <div th:fragment=\"content\" id=\"navigation-div\">\n" +
                    "            <ul class=\"nav nav-list\">");

            ps.println("<li id=\"menu_1\" class=\"menu-level-1\">\n" +
                    "\t\t<a href=\"javascript:void(0)\" class=\"dropdown-toggle\">\n" +
                    "\t\t\t<i></i>\n" +
                    "\t\t\t<span class=\"menu-text\">系统管理 </span>\n" +
                    "\t\t\t<b class=\"arrow icon-angle-down\"></b>\n" +
                    "\t\t</a>\n" +
                    "\n" +
                    "\t\t<ul class=\"submenu\">\n" );

            for(ClassEntity e : entities) {
                ps.println("\t\t\t\t<li class=\"menu-level-1\">\n" +
                        "\t\t\t\t\t<a href=\"/"+CommonKit.lowcaseFirst(e.getClassName())+"/list\">\n" +
                        "\t\t\t\t\t\t<i class=\"fa fa-angle-right\"></i> "+e.getClassShowName()+"管理 \n" +
                        "\t\t\t\t\t</a>\n" +
                        "\t\t\t\t</li>");
            }

            ps.println(
                    "\t\t\t\n" +
                    "\t\t</ul>\n" +
                    "\t</li>");

            ps.println("</ul>\n</div>\n" +
                    "    </body>\n" +
                    "</html>");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(ps!=null) {ps.close();}
        }
    }

    private void generateNav(String path, ClassEntity entity) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(path+"/"+"nav.html"), true, "UTF-8");
            ps.println("<!DOCTYPE html>");
            ps.println("<html xmlns=\"http://www.w3.org/1999/xhtml\"");
            ps.println("xmlns:th=\"http://www.thymeleaf.org\">");
            ps.println("<body>");
            ps.println("<div th:fragment=\"content\">");
            ps.println("<div class=\"ace-settings-container\" id=\"ace-settings-container\">");
            ps.println("<ul class=\"pagination\">");
            ps.println("<li><a class=\"auth1\" sn=\""+entity.getClassName()+"Controller.list\" href=\"/"+CommonKit.lowcaseFirst(entity.getClassName())+"/list\"><span class=\"fa fa-list\"></span> "+entity.getClassShowName()+"列表</a></li>");
            ps.println("<li><a class=\"auth1\" sn=\""+entity.getClassName()+"Controller.add\" href=\"/"+CommonKit.lowcaseFirst(entity.getClassName())+"/add\"><span class=\"fa fa-plus\"></span> 添加"+entity.getClassShowName()+"</a></li>");
            ps.println("</ul></div></div></body></html>");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(ps!=null) ps.close();
        }
    }

    private String generatePath(String path,String artifactId,ClassEntity entity) {
        return generatePath(path, artifactId, CommonKit.lowcaseFirst(entity.getClassName()));
    }

    private String generatePath(String path,String artifactId,String name) {
        String npath = path+"/"+artifactId;
        npath = npath+"/src/main/resources/templates/"+name;
        File f = new File(npath);
        if(!f.exists())
            f.mkdirs();
        return npath;
    }
}
