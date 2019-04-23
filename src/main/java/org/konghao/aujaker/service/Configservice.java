package org.konghao.aujaker.service;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.konghao.aujaker.kit.CommonKit;
import org.konghao.aujaker.model.FinalValue;
import org.konghao.aujaker.tools.ConfigTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Service
public class Configservice implements IConfigService {
	
	private final static String BASE_URL = "baseSrc";
	private final static String BASE_VIEW_URL = "baseView";

	@Autowired
	private ConfigTools configTools;

	@Override
	public void copyBaseSrc(String path,String artifactId) {
		try {
//			File f = new File(RepositoryService.class.getClassLoader().getResource(BASE_URL).getFile());
			File f = new File(configTools.getUploadPath("/base")+BASE_URL);
			File[] files = f.listFiles();
			for(File file:files) {
				String name = file.getName();
				File dest = new File(path+"/"+artifactId+"/src/main/java/"+name);
				if(file.isFile()) {
					if(!dest.exists()) dest.createNewFile();
					FileUtils.copyFile(file, dest);
				} else {
					if(!dest.exists()) dest.mkdirs();
					FileUtils.copyDirectory(file, dest);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void copyBaseView(String path,String artifactId) {
		try {
//			File f = new File(RepositoryService.class.getClassLoader().getResource(BASE_VIEW_URL).getFile());
			File f = new File(configTools.getUploadPath("/base")+BASE_VIEW_URL);
			File[] files = f.listFiles();
			for(File file:files) {
				String name = file.getName();
				File dest = new File(path+"/"+artifactId+"/src/main/resources/"+name);
				if(file.isFile()) {
					if(!dest.exists()) dest.createNewFile();
					FileUtils.copyFile(file, dest);
				} else {
					if(!dest.exists()) dest.mkdirs();
					FileUtils.copyDirectory(file, dest);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void generateApplicatoinPropertiesByProp(String path, String propFile) {
		Map<String,String> configs = readConfigByProperties(propFile);
		generateApplicationPropertiesByMap(path,configs);
	}

	private void generateApplicationPropertiesByMap(String path, Map<String, String> configs) {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
//		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			String database = configs.get("{dataType}");
//			System.out.println(database);
			String tfile = null;
			if("mysql".equals(database)) {
				tfile = "application_mysql.templates";
			} else if("sqlite3".equals(database)) {
				tfile = "application_sqlite3.templates";
				String dbname = configs.get("{dataname}");
				String filePath = path+"/"+configs.get("{artifactId}")+"/src/main/resources/";
				File file = new File(filePath);
				if(!file.exists()) file.mkdirs();
				
				File dbFile = new File(filePath+dbname);
				if(!dbFile.exists()) dbFile.createNewFile();
			}
			br = new BufferedReader(new InputStreamReader(
						(new Configservice().getClass().getClassLoader()
								.getResourceAsStream
								(tfile))));
			String str = null;
			while((str=br.readLine())!=null) {
				sb.append(str+"\n");
			}
			Set<String> keys = configs.keySet();
			String fileStr = sb.toString();
			for(String key:keys) {
				if(fileStr.indexOf(key)>=0) {
					if(key.equals("{package}")) {
						//如果是package，需要加.model。默认的实体类在model中
						fileStr = fileStr.replace(key, configs.get(key)+".model");
					} else {
						fileStr = fileStr.replace(key, configs.get(key));
					}
					
				}
			}
			String nPath = path+"/"+configs.get("{artifactId}")+"/src/main/resources";
			File f = new File(nPath);
			if(!f.exists()) f.mkdirs();
//			fw = new FileWriter(f+"/application.properties");
//			fw.write(fileStr);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f+"/application.properties"), "UTF-8"));
			bw.write(fileStr);
			bw.flush();
 		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(br!=null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(bw!=null) bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}


	private Map<String, String> readConfigByProperties(String propFile) {
		Properties prop = CommonKit.readProperties(propFile);
		Map<String,String> cfgs = new HashMap<String,String>();
		String type = prop.getProperty("database.type");
		cfgs.put("{artifactId}", prop.getProperty("maven.artifactId"));
		cfgs.put("{dataType}", type);
		cfgs.put("{package}", prop.getProperty("package"));
		cfgs.put("{url}", prop.getProperty("database.url"));
		cfgs.put("{driver}", prop.getProperty("database.driver"));
		cfgs.put("{dataname}", prop.getProperty("database.name"));
		if(type.equals("mysql")) {
			//如果是mysql，需要读取数据库的用户名和密码
			cfgs.put("{username}", prop.getProperty("database.username"));
			cfgs.put("{password}", prop.getProperty("database.password"));
		} else if(type.equals("sqlite3")) {
		}
		
		return cfgs;
	}

	@Override
	public void generateApplicationPropertiesByXml(String path, String xmlFile) {
		Map<String,String> configs = readConfigByXml(xmlFile);
		generateApplicationPropertiesByMap(path,configs);
	}

	@Override
	public void generateApplicationPropertiesByUploadXml(String path, String uploadFile) {
		Map<String,String> configs = readConfigByXml(new File(uploadFile));
		generateApplicationPropertiesByMap(path,configs);
	}

	private Map<String,String> readConfigByXml(File xmlFile) {
		SAXReader reader = new SAXReader();
		Map<String, String> configs = new HashMap<String,String>();
		try {
			Document d = reader.read(xmlFile);
			Element root = d.getRootElement();
			Element db = root.element("database");
			Element maven = root.element("maven");
			String groupId = maven.attributeValue("groupId");
			String artifactId = maven.attributeValue("artifactId");
			String databaseType = db.attributeValue("type");
			String dataName = db.attributeValue("name");
			Element username = db.element("username");
			Element password = db.element("password");
			Element url = db.element("url");
			Element driver = db.element("driver");
			configs.put("{artifactId}",artifactId);
			configs.put("{dataType}", databaseType);
			configs.put("{package}",groupId);
			configs.put("{url}",url.getText());
			configs.put("{driver}",driver.getText());
			configs.put("{dataname}",dataName);
			if(databaseType.equals("mysql")) {
				configs.put("{username}", username.getText());
				configs.put("{password}", password.getText());
			} else if(databaseType.equals("sqlite3")) {
			}
			return configs;
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<String,String> readConfigByXml(String xmlFile) {
		return readConfigByXml(new File(Configservice.class.getClassLoader().getResource(xmlFile).getFile()));
	}
	@Override
	public void generatePomByProp(String path, String propFile) {
		Map<String,String> configs = readPomByProperties(propFile);
		generateApplicationPomByMap(path,configs);
	}

	private Map<String, String> readPomByProperties(String propFile) {
		Properties prop = CommonKit.readProperties(propFile);
		Map<String,String> cfgs = new HashMap<String,String>();
		String type = prop.getProperty("database.type");
		cfgs.put("{groupId}", prop.getProperty("maven.groupId"));
		cfgs.put("{artifactId}", prop.getProperty("maven.artifactId"));
		cfgs.put("{driver}", prop.getProperty("database.driver"));
		if(type.equals("mysql")) {
			cfgs.put("{databaseDriverConnection}", MYSQL_DEP);
		} else if(type.equals("sqlite3")) {
			cfgs.put("{databaseDriverConnection}", SQLITE_DEP);
		}
		
		return cfgs;
	}

	private void generateApplicationPomByMap(String path, Map<String, String> configs) {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
//		FileWriter fw = null;
		BufferedWriter bw = null;

		try {
			String tfile = "pom.templates";
			br = new BufferedReader(new InputStreamReader(
						(Configservice.class.getClassLoader()
								.getResourceAsStream
								(tfile))));
			String str = null;
			while((str=br.readLine())!=null) {
				sb.append(str+"\n");
			}
			Set<String> keys = configs.keySet();
			String fileStr = sb.toString();
			for(String key:keys) {
				if(fileStr.indexOf(key)>=0) {
					fileStr = fileStr.replace(key, configs.get(key));
				}
			}
			String nPath = path+"/"+configs.get("{artifactId}");
			File f = new File(nPath);
			if(!f.exists()) f.mkdirs();
//			fw = new FileWriter(f+"/pom.xml");
//			fw.write(fileStr);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f+"/pom.xml"), "UTF-8"));
			bw.write(fileStr);
			bw.flush();
 		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(br!=null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(bw!=null) bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void generatePomByXml(String path, String xmlFile) {
		Map<String,String> configs = readPomByXml(xmlFile);
		generateApplicationPomByMap(path, configs);
	}

	@Override
	public void generatePomByUploadXml(String path, String uploadFile) {
		Map<String,String> configs = readPomByXml(new File(uploadFile));
		generateApplicationPomByMap(path, configs);
	}

	private Map<String, String> readPomByXml(File xmlFile) {
		SAXReader reader = new SAXReader();
		Map<String, String> configs = new HashMap<String,String>();
		try {
			Document d = reader.read(xmlFile);
			Element root = d.getRootElement();
			Element db = root.element("database");
			Element maven = root.element("maven");
			String groupId = maven.attributeValue("groupId");
			String artifactId = maven.attributeValue("artifactId");
			String databaseType = db.attributeValue("type");
			configs.put("{artifactId}",artifactId);
			configs.put("{dataType}", databaseType);
			configs.put("{groupId}",groupId);
//			System.out.println(databaseType);
			if(databaseType.equals("mysql")) {
				configs.put("{databaseDriverConnection}", MYSQL_DEP);
			} else if(databaseType.equals("sqlite3")) {
				configs.put("{databaseDriverConnection}", SQLITE_DEP);
			}
			return configs;
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<String, String> readPomByXml(String xmlFile) {
		return readPomByXml(new File(Configservice.class.getClassLoader().getResource(xmlFile).getFile()));
	}

	@Override
	public void generateApplicationConfig(String path,String groupId,String artifactId) {
		String npath = CommonKit.generateApplicationPath(path, groupId, artifactId);
//		String afile = CommonKit.upcaseFirst(artifactId)+"Application.java";
		String afile = "AujakerDemoApplication.java";
		StringBuffer sb = new StringBuffer();
		sb.append("package ").append(groupId).append(";\n");
		sb.append("import org.konghao.reposiotry.base.BaseRepositoryFactoryBean;\n");
		sb.append("import org.springframework.boot.SpringApplication;\n");
		sb.append("import org.springframework.data.jpa.repository.config.EnableJpaRepositories;\n");
		sb.append("import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;\n" +
				"import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;\n" +
				"import org.springframework.boot.web.servlet.ErrorPage;\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.http.HttpStatus;");
		sb.append("import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n");
		sb.append("@EnableJpaRepositories(basePackages = {\"").append(groupId).append("\"},\n")
			.append("\trepositoryFactoryBeanClass = BaseRepositoryFactoryBean.class//指定自己的工厂\n");
		sb.append(")\n");
		sb.append("@SpringBootApplication\n");
//		sb.append("public class ").append(CommonKit.upcaseFirst(artifactId)).append("Application {\n");
		sb.append("public class AujakerDemoApplication {\n");
		sb.append("\tpublic static void main(String[] args) {\n");
//		sb.append("\t\tSpringApplication.run("+CommonKit.upcaseFirst(artifactId)+"Application.class,args);\n");
		sb.append("\t\tSpringApplication.run(AujakerDemoApplication.class,args);\n");
		sb.append("\t}\n");

		sb.append("\n\t@Bean\n" +
				"    public EmbeddedServletContainerCustomizer containerCustomizer() {\n" +
				"        return new EmbeddedServletContainerCustomizer(){\n" +
				"            @Override\n" +
				"            public void customize(ConfigurableEmbeddedServletContainer container) {\n" +
				"                container.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, \"/400\"));\n" +
				"                container.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, \"/500\"));\n" +
				"                container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, \"/404\"));\n" +
				"            }\n" +
				"        };\n" +
				"    }\n");

		sb.append("}\n");
		
//		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
//			fw = new FileWriter(npath+"/"+afile);
//			fw.write(sb.toString());
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(npath+"/"+afile), "UTF-8"));
			bw.write(sb.toString());
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(bw!=null) bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void generateExcelApplicationConfig(String path) {
		Map<String,String> configs = new HashMap<String,String>();
		configs.put("{artifactId}",FinalValue.EXCEL_ARTIFACTID);
		configs.put("{dataType}", FinalValue.EXCEL_DB);
		configs.put("{package}",FinalValue.EXCEL_GROUPID);
		configs.put("{url}",FinalValue.EXCEL_DB_URL);
		configs.put("{driver}",FinalValue.EXCEL_DB_DRIVER);
		configs.put("{dataname}", FinalValue.EXCEL_DB_NAME);
		generateApplicationPropertiesByMap(path,configs);
	}

	@Override
	public void generateExcelPomConfig(String path) {
		Map<String,String> configs = new HashMap<String,String>();
		configs.put("{artifactId}",FinalValue.EXCEL_ARTIFACTID);
		configs.put("{dataType}", FinalValue.EXCEL_DB);
		configs.put("{groupId}",FinalValue.EXCEL_GROUPID);
		configs.put("{databaseDriverConnection}", SQLITE_DEP);
		configs.put("{dataname}", FinalValue.EXCEL_DB_NAME);
		generateApplicationPomByMap(path, configs);
	}

}
