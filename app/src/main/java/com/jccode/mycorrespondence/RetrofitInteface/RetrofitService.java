package com.jccode.mycorrespondence.RetrofitInteface;


import com.jccode.mycorrespondence.models.CorrespondenceModel;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitService {

    @POST("shareCorrespondence")
    Call<CorrespondenceModel> getCheckSum(@Query("userId") String uId,
                                          @Query("shareTo") String shareUserId,
                                          @Query("cId") String cId);
}
