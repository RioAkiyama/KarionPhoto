package com.rio.aki.api;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.api.client.util.ArrayMap;
import com.google.photos.types.proto.MediaItem;
import com.google.photos.types.proto.MediaMetadata;
import com.google.protobuf.Timestamp;
import com.rio.aki.CommonUtils;
import com.rio.aki.PhotosLibraryClientFactory;
import com.rio.aki.ResponseMessage;
import com.rio.aki.domain.service.HomeService;
import com.rio.aki.domain.service.IHomeService;

@RestController
@RequestMapping("/api")
public class HomeRestController {
	@Value("${photo.search.startdate}")
	private String PhotoSearchStartDate;
	
	private PhotosLibraryClientFactory mPhotosLibraryClientFactory;
	
	HomeRestController(@Value("${photo.search.startdate}") String photoSearchStartDate, @Value("${credential_path}") String CredentialPath) {
		this.mPhotosLibraryClientFactory = new PhotosLibraryClientFactory(CredentialPath);
		try {
			this.mPhotosLibraryClientFactory.setRandomSearchDate(photoSearchStartDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@PostMapping("/getPhotoUrl")
	public String getPhotoUrl() {
		try {
			ResponseMessage result = new ResponseMessage().setData("result", "OK");
			
			Iterable<MediaItem> photos = this.mPhotosLibraryClientFactory.getPhotos();
			if(photos == null) {
				result.setData("authurl", PhotosLibraryClientFactory.AuthUrl);
				return result.getResponse();
			}
			
			List<Map<String, String>> photoData = new ArrayList<>();
			for(MediaItem photo : photos) {
				if(photo.getMimeType().indexOf("video") != -1) continue;
				
				Map<String, String> data = new ArrayMap<>();
				data.put("src", photo.getBaseUrl());
				
				String creationDate = " - ";
				MediaMetadata metadata = photo.getMediaMetadata();
				if(metadata != null) {
					Timestamp timestamp = metadata.getCreationTime();
					if(timestamp != null) {
						creationDate = timestamp.toString();
						creationDate = creationDate.replace("seconds: ", "");
						creationDate = creationDate.replace("\n", "");
						creationDate = String.valueOf(Long.parseLong(creationDate) * 1000);
					}
				} 
				data.put("date", creationDate);
				data.put("type", photo.getMimeType());
				photoData.add(data);
			}
			
			result.setData("StartDate", PhotosLibraryClientFactory.StartDate);
			result.setData("EndDate", PhotosLibraryClientFactory.EndDate);
			result.setData("photos", photoData);
			return result.getResponse();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		return new ResponseMessage().setData("result", "NG").getResponse();
	}
	
	@PostMapping("/updateDate")
	public String updateDate(Model model, @RequestBody String data) {
		try {
			JSONObject jsonObj = new JSONObject(data);
			if(jsonObj.has("startdate") && !jsonObj.isNull("startdate") && jsonObj.has("enddate") && !jsonObj.isNull("enddate")) {
				java.util.Date startdate = CommonUtils.strToDate(jsonObj.getString("startdate"));
				java.util.Date enddate = CommonUtils.strToDate(jsonObj.getString("enddate"));
				PhotosLibraryClientFactory.StartDate = startdate;
				PhotosLibraryClientFactory.EndDate = enddate;
			}else {
				this.mPhotosLibraryClientFactory.setRandomSearchDate(PhotoSearchStartDate);
			}
			
			return new ResponseMessage().setData("result", "OK").getResponse();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new ResponseMessage().setData("result", "NG").getResponse();
	}
}
