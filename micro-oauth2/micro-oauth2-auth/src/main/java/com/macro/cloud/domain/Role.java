package com.macro.cloud.domain;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.util.Date;

@Data
@TableName("t_role")
public class Role {
    private Integer id;
    private String roleName;
    private String description;
    private Date createTime;
    private Date updateTime;
    private Integer status;

}
