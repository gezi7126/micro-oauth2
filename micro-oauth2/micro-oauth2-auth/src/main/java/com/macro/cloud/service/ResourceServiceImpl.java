package com.macro.cloud.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.macro.cloud.constant.RedisConstant;
import com.macro.cloud.domain.Resource;
import com.macro.cloud.domain.Role;
import com.macro.cloud.domain.RoleResource;
import com.macro.cloud.mapper.ResourceMapper;
import com.macro.cloud.mapper.RoleMapper;
import com.macro.cloud.mapper.RoleResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 资源与角色匹配关系管理业务类
 * Created by macro on 2020/6/19.
 */
@Service
public class ResourceServiceImpl {

    private Map<String, List<String>> resourceRolesMap;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private RoleResourceMapper roleResourceMapper;

    @PostConstruct
    public void initData() {
        //初始化时先清空token
        redisTemplate.delete(RedisConstant.JWT_TOKEN);
        redisTemplate.delete(RedisConstant.RESOURCE_ROLES_MAP);

        resourceRolesMap = new TreeMap<>();

        List<Resource> resourceList=resourceMapper.selectList(new QueryWrapper<Resource>());

        resourceList.forEach((resource)->{
            QueryWrapper<RoleResource> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("permission_id",resource.getId());
            List<RoleResource> roleResources=roleResourceMapper.selectList(queryWrapper);
            List<String> roleNames=roleResources.stream().map((roleResource)->{
               Role r= roleMapper.selectById(roleResource.getRoleId());
               return r.getRoleName();
            }).collect(Collectors.toList());
            resourceRolesMap.put(resource.getUrl(),roleNames);
        });
        /*resourceRolesMap.put("/api/hello", CollUtil.toList("ADMIN"));
        resourceRolesMap.put("/api/user/currentUser", CollUtil.toList("ADMIN", "TEST"));*/
        redisTemplate.opsForHash().putAll(RedisConstant.RESOURCE_ROLES_MAP, resourceRolesMap);

    }
}
