package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    public BrandMapper brandMapper;

    /**
     * 分页查询品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //实现模糊查询
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //根据名称或者首字母查询
        if(StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%" + key + "%").orLike("letter", key);
        }

        if(StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }

        //进行分页，排序
        PageHelper.startPage(page, rows);
        List<Brand> brands = brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 保存品牌信息
     * @param brand
     * @param cids
     */
    @Transactional(rollbackFor = Exception.class) //需要开启事务，保证原子性
    public void saveBrand(Brand brand, List<Long> cids) {
        this.brandMapper.insertSelective(brand);

        // 通用mapper只能操作单张表，所以中间表添加数据需要自定义
        cids.forEach(cid -> {
            this.brandMapper.insertBrandAndCategory(cid, brand.getId());
        });
    }

    /**
     * 根据ID查询品牌
     * @param bid
     * @return
     */
    public Brand queryById(Long bid) {
        return this.brandMapper.selectByPrimaryKey(bid);
    }

    /**
     * 根据cid查询对应的品牌列表
     * @param cid
     * @return
     */
    public List<Brand> queryBrandByCid(Long cid) {
        return this.brandMapper.queryBrandByCid(cid);
    }
}
