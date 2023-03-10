package com.jeesite.common.ueditor.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jeesite.common.image.ImageUtils;
import com.jeesite.common.io.FileUtils;
import com.jeesite.common.media.VideoUtils;
import com.jeesite.common.ueditor.PathFormat;
import com.jeesite.common.ueditor.define.ActionMap;
import com.jeesite.common.ueditor.define.AppInfo;
import com.jeesite.common.ueditor.define.BaseState;
import com.jeesite.common.ueditor.define.FileType;
import com.jeesite.common.ueditor.define.State;

public class BinaryUploader {

	public static final State save(HttpServletRequest request,
			Map<String, Object> conf) {
		
		String contentType = request.getContentType();
		if (!("POST".equals(request.getMethod()) && contentType != null
				&& contentType.startsWith("multipart/"))) {
			return new BaseState(false, AppInfo.NOT_MULTIPART_CONTENT);
		}

		try {
			MultipartFile file = null;
			if (request instanceof MultipartHttpServletRequest){
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				Iterator<String> it = multiRequest.getFileNames();
				while (it.hasNext()) {
					MultipartFile f = multiRequest.getFile(it.next());
					if (f != null && !f.isEmpty() && f.getOriginalFilename() != null) {
						file = f;
					}
					break;
				}
			}
			if (file == null) {
				return new BaseState(false, AppInfo.NOTFOUND_UPLOAD_DATA);
			}

			String savePath = (String) conf.get("savePath");
			String originFileName = file.getOriginalFilename();
			String suffix = FileType.getSuffixByFilename(originFileName);

			originFileName = originFileName.substring(0,
					originFileName.length() - suffix.length());
			savePath = savePath + suffix;

			long maxSize = ((Long) conf.get("maxSize")).longValue();

			if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
				return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
			}

			savePath = PathFormat.parse(savePath, originFileName);

			String physicalPath = FileUtils.path((String) conf.get("rootPath") + savePath);

			InputStream is = null;
			State storageState = null;
			try {
				is = file.getInputStream();
				storageState = StorageManager.saveFileByInputStream(is, physicalPath, maxSize);
			} finally {
				if (is != null) {
					is.close();
				}
			}

			if (storageState != null && storageState.isSuccess()) {
				int actionCode = ((Integer) conf.get("actionCode")).intValue();
				String ctx = request.getContextPath(); // ThinkGem ??????????????????????????????contextpath??????
				
				// ????????????????????????????????????
				if (actionCode == ActionMap.UPLOAD_IMAGE){
					
					// ???????????????????????????
					if ((Boolean)conf.get("imageCompressEnable")){
						Integer maxWidth = (Integer)conf.get("imageCompressBorder");
						ImageUtils.thumbnails(new File(physicalPath), maxWidth, -1, null);
					}
					
				}
				
				// ??????????????? ???????????? ???????????????????????? ???????????? ThinkGem
				else if(actionCode == ActionMap.UPLOAD_VIDEO){
					final VideoUtils v = new VideoUtils(physicalPath);
					// ????????? 
					if (v.cutPic()){
						// ????????????????????????????????????
						Thread thread = new Thread("video-convert") {
							@Override
							public void run() {
								try {
									Thread.sleep(5000);
									v.convert();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						};
						thread.setDaemon(true);
						thread.start();  
						storageState.putInfo("url", ctx + PathFormat.format(savePath) + "." + v.getOutputFileExtension());
						storageState.putInfo("type", "." + v.getOutputFileExtension());
						storageState.putInfo("original", originFileName +"."+ v.getInputFileExtension());
						
						// Ueditor??????????????????????????????????????????
						StorageManager.uploadFileSuccess(physicalPath, storageState);
						
						return storageState;
					}
				}
				storageState.putInfo("url", ctx + PathFormat.format(savePath));
				storageState.putInfo("type", suffix);
				storageState.putInfo("original", originFileName + suffix);
				
				// UEditor?????????????????????????????????
				StorageManager.uploadFileSuccess(physicalPath, storageState);
			}

			return storageState;
		} catch (IOException e) {
			return new BaseState(false, AppInfo.IO_ERROR);
		}
	}

	private static boolean validType(String type, String[] allowTypes) {
		List<String> list = Arrays.asList(allowTypes);

		return list.contains(type);
	}
	
}
