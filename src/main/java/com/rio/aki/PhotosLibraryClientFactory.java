package com.rio.aki;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.common.collect.ImmutableList;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.proto.ContentFilter;
import com.google.photos.library.v1.proto.DateFilter;
import com.google.photos.library.v1.proto.Filters;
import com.google.photos.library.v1.proto.ListAlbumsRequest;
import com.google.photos.library.v1.proto.ListAlbumsResponse;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest.Builder;
import com.google.photos.library.v1.proto.SearchMediaItemsResponse;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.DateRange;
import com.google.photos.types.proto.MediaItem;
import com.google.type.Date;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

public class PhotosLibraryClientFactory {
	private static final java.io.File DATA_STORE_DIR = new java.io.File(PhotosLibraryClientFactory.class.getResource("/").getPath(), "credentials");
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final int LOCAL_RECEIVER_PORT = 8081;
	
	public static java.util.Date StartDate;
	public static java.util.Date EndDate;
	public static PhotosLibraryClient Client;
	public static String AuthUrl;
	
	public String mCredentialsPath;
	
	public PhotosLibraryClientFactory(String credentialsPath) {
		this.mCredentialsPath = credentialsPath;
	}

	public boolean init() throws IOException, GeneralSecurityException {
		if(Client != null) return true;
		
		Credentials credentials = getUserCredentials();
		if(credentials == null) return false;
		
		PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder()
	            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
	            .build();
		
		Client = PhotosLibraryClient.initialize(settings);
		return true;
	}
		
	private Credentials getUserCredentials() throws IOException, GeneralSecurityException {
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(this.mCredentialsPath)));
		String clientId = clientSecrets.getDetails().getClientId();
		String clientSecret = clientSecrets.getDetails().getClientSecret();
		
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
			GoogleNetHttpTransport.newTrustedTransport(),
			JSON_FACTORY,
			clientSecrets,
			ImmutableList.of("https://www.googleapis.com/auth/photoslibrary.readonly")
		)
			.setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
			.setAccessType("offline")
			.build();
		
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(LOCAL_RECEIVER_PORT).build();
		
		Credential credential = flow.loadCredential("user");
		if (credential != null && (
				credential.getRefreshToken() != null ||
				credential.getExpiresInSeconds() == null ||
				credential.getExpiresInSeconds() > 60)) {
			return UserCredentials.newBuilder()
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setRefreshToken(credential.getRefreshToken())
					.build();
		}
		
		String redirectUri = receiver.getRedirectUri();
		AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);
		AuthUrl = authorizationUrl.build();
		return null;
	}
	
	public void setRandomSearchDate(String stDateStr) throws ParseException {
		java.util.Date startDate = CommonUtils.strToDate(stDateStr);
		int diffDateCnt = CommonUtils.diffDate(startDate, new java.util.Date());
		int randomCnt = new Random().nextInt(diffDateCnt);
		StartDate = CommonUtils.addDate(startDate, randomCnt);
		EndDate = CommonUtils.addDate(startDate, randomCnt + 60);
	}
	
	public List<Album> getAlbums() throws IOException, GeneralSecurityException {
		if(!init()) return null;
		
		ListAlbumsRequest request = ListAlbumsRequest.getDefaultInstance();
		ListAlbumsResponse response = PhotosLibraryClientFactory.Client.listAlbumsCallable().call(request);
		return response.getAlbumsList();
	}
	
	public Iterable<MediaItem> getPhotos() throws IOException, GeneralSecurityException {
		if(!init()) return null;
		
		Builder builder = SearchMediaItemsRequest.newBuilder();
		if(StartDate != null && EndDate != null) {
			DateFilter dateFilter = DateFilter.newBuilder().addRanges(DateRange.newBuilder()
					        .setStartDate(CommonUtils.convertToGoogleDate(StartDate))
					        .setEndDate(CommonUtils.convertToGoogleDate(EndDate)).build()).build();
			Filters filter = Filters.newBuilder().setDateFilter(dateFilter).build();
			builder.setFilters(filter);
		}
		
		SearchMediaItemsRequest request = builder.build();
		SearchMediaItemsResponse response = PhotosLibraryClientFactory.Client.searchMediaItemsCallable().call(request);
		return response.getMediaItemsList();
	}
	
}
