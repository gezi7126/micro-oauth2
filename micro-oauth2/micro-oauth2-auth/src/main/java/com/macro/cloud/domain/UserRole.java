package com.macro.cloud.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_user_role")
public class UserRole {
   private Integer userId;
   private Integer roleId;
   private Date createTime;
   private String creator;
}
