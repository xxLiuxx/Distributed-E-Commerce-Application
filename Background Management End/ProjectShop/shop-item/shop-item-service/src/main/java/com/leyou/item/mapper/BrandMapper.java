package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface BrandMapper extends Mapper<Brand> {

    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) VALUES (#{cid}, #{bid})")
    void insertBrandAndCategory(@Param("cid") Long cid, @Param("bid") Long bid);

    @Select("SELECT * FROM tb_brand t1 JOIN tb_category_brand t2 ON t1.id = t2.brand_id WHERE t2.category_id = #{cid}")
    //@Select("select * from tb_brand where id in (select brand_id from tb_category_brand where category_id = #{cid})")
    List<Brand> queryBrandByCid(@Param("cid") Long cid);
}
