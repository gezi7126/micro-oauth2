package com.macro.cloud.domain;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@Component
@TableName("t_user")
public class Users implements Serializable {
    private Long id;
    private String username;
    private String password;
    //手机号
    private String mobile;
    //用户姓名
    private String fullname;

    @TableField(exist = false)
    private List<String> roles;

    public Users(String username,String password){
        this.username=username;
        this.password=password;
    }

}
