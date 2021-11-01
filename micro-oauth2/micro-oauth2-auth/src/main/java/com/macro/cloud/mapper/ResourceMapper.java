package com.macro.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macro.cloud.domain.Resource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
}
