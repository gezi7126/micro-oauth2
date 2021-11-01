package com.macro.cloud.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_permission")
public class Resource {
    private Integer id;
    private String code;
    private String description;
    private String url;
}
