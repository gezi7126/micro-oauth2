package com.macro.cloud.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_role_permission")
public class RoleResource {
    private Integer roleId;
    private Integer permissionId;
}
