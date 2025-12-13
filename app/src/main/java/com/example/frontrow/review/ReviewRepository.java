package com.example.frontrow.review;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public static void getAllReviews(Callback<List<ReviewItem>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReviewItem> reviewList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ReviewItem item = ReviewItem.fromDoc(doc);
                        if (item != null) {
                            reviewList.add(item);
                        }
                    }
                    callback.onSuccess(reviewList);
                })
                .addOnFailureListener(callback::onError);
    }

    public static void getReviewsByAuthor(String authorUid, Callback<List<ReviewItem>> callback) {
        if (authorUid == null || authorUid.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .whereEqualTo("authorUid", authorUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReviewItem> reviewList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ReviewItem item = ReviewItem.fromDoc(doc);
                        if (item != null) {
                            reviewList.add(item);
                        }
                    }
                    callback.onSuccess(reviewList);
                })
                .addOnFailureListener(callback::onError);
    }
}
