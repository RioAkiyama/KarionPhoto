package com.rio.aki.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;
import com.google.type.Date;
import com.rio.aki.CommonUtils;
import com.rio.aki.PhotosLibraryClientFactory;
import com.rio.aki.domain.service.HomeService;
import com.rio.aki.domain.service.IHomeService;

@Controller
public class HomeController {
	@Value("${credential_path}") 
	private String credential_path;
	
	public HomeController() {
	}
	
	@RequestMapping(value = {"/", "/photos"})
	public String photos(Model model) {
		return "photos";
	}
	
}
