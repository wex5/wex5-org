package org;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.alibaba.fastjson.JSONObject;
import com.justep.baas.action.ActionContext;


public class PersonAvatar {
	private static String[] allFiles = { "image/vnd.dwg", "image/vnd.dxf", "image/gif", "image/jp2", "image/jpeg", "image/png", "image/vnd.svf", "image/tiff"};
	/**************************************************************************************************************************/
	static String docStorePath =  Thread.currentThread().getContextClassLoader().getResource("").getPath() + ".." + File.separator + ".."+ File.separator + "chatHeadPortrait";
	/**************************************************************************************************************************/
	static File docStoreDir;
	private static final String DATASOURCE_X5SYS = "system";
	private static List<String> list = Arrays.asList(allFiles);
	
	/**
	 * 获取文档存储路径，接受
	 * */
	/**************************************************************************************************************************/
	public static String getDocStorePath(String path){
		return docStoreDir.getAbsolutePath()+File.separator+path;
	}
	
	public static boolean removeFile(String path){
		//删除状态
		boolean state = false;
		//获取头像文件路径的对象
		File file = new File( getDocStorePath(path));
		//获取上一级目录
		File parentFile = file.getParentFile();
		
		//判断是否为文件夹
		if(parentFile.isDirectory()){
			//获取文件夹下所有文件列表
			File[] files = parentFile.listFiles();
			//遍历删除文件
			for(File delFile : files){
				delFile.delete();
			}
			state = parentFile.delete();
		}
		
		return state;
	}
	/**************************************************************************************************************************/
	
	private static void crateDir(){
//		String baasPath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + ".." + File.separator + "..";
//		docStorePath = baasPath + File.separator + "chatHeadPortrait";
		File file = new File(docStorePath);
		//兼容以前存储目录
		if(file.exists() && file.isDirectory()){
			docStoreDir = file;
		}else{
			file.mkdir();
			File  newFile = new File(docStorePath);
			docStoreDir = newFile;
		}
	}
	public static JSONObject personAvatar(JSONObject params, ActionContext context) throws SQLException, NamingException {
		crateDir();//判断是否存在上传头像的文件，没有则创建
		HttpServletRequest req = (HttpServletRequest) context.get(ActionContext.REQUEST);
		Connection conn = context.getConnection(DATASOURCE_X5SYS);
		JSONObject ret = new JSONObject();
 			try {
				 String imgUrl = saveFile(req,conn);
				 ret.put("imgFile", imgUrl);
			} catch (FileUploadException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		return ret;
	}
	public static JSONObject getPersonAvatar(JSONObject params, ActionContext context) throws SQLException, NamingException{
		HttpServletResponse resp = (HttpServletResponse) context.get(ActionContext.RESPONSE);
		ServletOutputStream out;
		crateDir();//判断是否存在上传头像的文件，没有则创建
		String imgFile = params.getString("imgFile");
		File file = new File(docStoreDir.getAbsolutePath()+File.separator+imgFile);
		if(file.exists()){
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				out = resp.getOutputStream();
				int b = 0 ;
				byte[] buffer = new byte[1024*10];
				while((b = inputStream.read(buffer))!=-1){
					out.write(buffer,0,b);
				}
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}finally{
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	public static String saveFile(HttpServletRequest request,Connection conn) throws FileUploadException, IOException, SQLException {
		// 临时文件目录
		File tempPathFile = new File(System.getProperty("java.io.tmpdir"));
		if (!tempPathFile.exists()) {
			tempPathFile.mkdirs();
		}
		String imgName = null;
		String folder = null;
		String id = null;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(10 * 1024 * 1024);
		factory.setRepository(tempPathFile);
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(500 * 1024 * 1024); // 设置最大文件尺寸500M
		upload.setHeaderEncoding("UTF-8");
		@SuppressWarnings("unchecked")
		List<FileItem> items = upload.parseRequest(request);
		Iterator<FileItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			FileItem fi = (FileItem) iterator.next();
			String fieldName = fi.getFieldName();
			if (!fi.isFormField()&&list.contains(fi.getContentType())) {
				BufferedInputStream in = new BufferedInputStream(fi.getInputStream());
				imgName =createImgName(fi.getName());
				folder = createFolder();
				/**************************************************************************************************************************/
				FileOutputStream out = new FileOutputStream(new File(getDocStorePath(folder+File.separator+imgName)));
				/**************************************************************************************************************************/
				BufferedOutputStream output = new BufferedOutputStream(out);
				Streams.copy(in, output, true);
			} else if("id".equals(fieldName)){
				String fieldValue = fi.getString();
				try{
					id = new String(fieldValue.getBytes("ISO-8859-1"),"UTF-8");
				}catch (Exception e) {}

			}
		}
		if(id!=null&&imgName!=null){
			
			String selSql = "select sPhoto from sa_opperson where sID = '"+id+"'";
			
			Statement stmt = null;
			String sql = "update sa_opperson set sPhoto='"+folder+"/"+imgName+"' where sID= '"+id+"'" ;
			stmt = conn.createStatement();
			
			/**************************************************************************************************************************/
			/*
			 * 查询用户原有头像文件路径，并删除该文件
			 * */
			
			ResultSet rel = stmt.executeQuery(selSql);
			String filePath=null;
			while(rel.next()){
				filePath = rel.getString("sPhoto");
			}
			removeFile(filePath);//删除文件操作
			/**************************************************************************************************************************/
			
			int i = stmt.executeUpdate(sql);
			if(i>0){
				if(stmt!=null)
					stmt.close();
				if(conn!=null)
					conn.close();
				}
		}
		return folder+File.separator+imgName;
	}
	private static String createFolder(){
		String fileName = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
		File  file = new File(docStorePath+File.separator+fileName);
		if(!file .exists()  && !file .isDirectory()){
			file .mkdir();
		}
		return fileName;
	}
	private static String createImgName(String imgName){
		Random random = new Random();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = df.format(new Date()).toString();
		return time+random.nextInt(10000)+imgName;
	}
	
}
