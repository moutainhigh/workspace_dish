package com.wmt.wechat.dao.decoration;

import com.wmt.wechat.model.decoration.MaterialData;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2019 by CANSHU
 *
 * @author adu Create on 2019-01-03 20:17
 * @version 1.0
 */
@Repository("materialDao")
@Mapper
public interface MaterialDao {

    /**
     *
     * 店铺的装修材料
     * 数据以 MaterialData.showSeq 升序排列
     *
     * @return
     */
    @Select("SELECT * FROM material ORDER BY showSeq ASC LIMIT 100")
    public List<MaterialData> getPoiMaterialDataList();


    @Select("SELECT * FROM material WHERE id IN (${ids})")
    @MapKey("id")
    public Map<Integer,MaterialData> getPoiMaterialDataMap(@Param("ids") String ids);


    @Select("SELECT * FROM material WHERE id=#{id}")
    public MaterialData getMaterialData(int id);
}
