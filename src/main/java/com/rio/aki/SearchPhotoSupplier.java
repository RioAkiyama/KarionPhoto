package com.rio.aki;

import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest;
import com.google.photos.library.v1.proto.SearchMediaItemsResponse;
import com.google.photos.types.proto.MediaItem;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public class SearchPhotoSupplier implements Supplier<Iterable<MediaItem>> {
	private final PhotosLibraryClient mClient;
	private SearchMediaItemsRequest mBaseRequest;
	private Optional<SearchMediaItemsRequest> mRequest;

	public SearchPhotoSupplier(PhotosLibraryClient client, SearchMediaItemsRequest request) {
		this.mClient = client;
		this.mBaseRequest = request;
		this.mRequest = Optional.of(request);
	}

	public void refresh() {
		this.mRequest = Optional.of(this.mBaseRequest);
	}

	public void setBaseRequest(SearchMediaItemsRequest request) {
		this.mBaseRequest = request;
	}

	@Override
	public Iterable<MediaItem> get() {
		if (!mRequest.isPresent()) {
			return Collections.emptyList();
		}
		SearchMediaItemsResponse response = mClient.searchMediaItemsCallable().call(mRequest.get());
		if (response.getNextPageToken().isEmpty()) {
			mRequest = Optional.empty();
		} else {
			mRequest = Optional.of(mRequest.get().toBuilder().setPageToken(response.getNextPageToken()).build());
		}
		return response.getMediaItemsList();
	}
}
