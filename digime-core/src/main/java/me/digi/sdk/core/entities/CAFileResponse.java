/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CAFileResponse {

    @SerializedName("fileContent")
    public List<CAContent> fileContent;

    @SerializedName("fileList")
    public List<String> fileIds;
}
