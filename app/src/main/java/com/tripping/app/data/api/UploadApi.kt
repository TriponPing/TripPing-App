// [파일 설명] 이미지 업로드 서버 통신 담당. 사진 파일 하나를 보내고, 바로 쓸 수 있는 URL을 받음.
package com.tripping.app.data.api

import com.tripping.app.data.response.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {

    @Multipart
    @POST("uploads/images")
    suspend fun uploadImage(@Part file: MultipartBody.Part): UploadImageResponse
}
