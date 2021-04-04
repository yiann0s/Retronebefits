package com.yiann0s.retronebefits.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Car (
    @SerializedName("id")
    @Expose
    var id: String? = null,

    @SerializedName("name")
    @Expose
    var name: String? = null,

    @SerializedName("desc")
    @Expose
    var desc: String? = null

)