package com.jccode.mycorrespondence.Interface;

import com.jccode.mycorrespondence.models.CheckSumModel;
import com.jccode.mycorrespondence.models.ResponseModel;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitService {

    @POST("getCheckSUm")
    Call<CheckSumModel> getCheckSum(@Query("oId") String oId,
                                    @Query("custId") String custId,
                                    @Query("amount") String amount);


    @POST("shareCorrespondence")
    Call<Void> shareCorrespondence(@Query("userId") String uID,
                                   @Query("shareTo") String shareId,
                                   @Query("cId") String cId);

    @POST("shareDocuments")
    Call<Void> shareDocuments(@Query("userId") String uID,
                              @Query("shareTo") String shareId,
                              @Query("cId") String cId);


    @POST("getLiveSharedDocumts")
    Call<ResponseModel> shareLiveDocuments(@Query("userId") String uID,
                                           @Query("shareId") String shareId,
                                           @Query("docId") String docId,
                                           @Query("to") String to,
                                           @Query("from") String cId);


}

