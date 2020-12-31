package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * spu商品分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //判断key是否为空
        if(StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }

        //判断是否上架
        if(saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        //分页条件
        PageHelper.startPage(page, rows);

        //根据条件查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);

        //查询出的是spu，转换为spuBo
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();

            //将spu的属性复制给spuBo
            BeanUtils.copyProperties(spu, spuBo);

            //查询品牌名称
            Brand brand = this.brandService.queryById(spuBo.getBrandId());
            spuBo.setBname(brand.getName());

            //根据cid1, cid2, cid3查询cname和bname
            List<String> cnames = this.categoryService.queryCategoryByIdList(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(cnames, "-"));

            return spuBo;
        }).collect(Collectors.toList());

        //返回结果
        return new PageResult<>(spuPageInfo.getTotal(), spuBos);
    }

    /**
     * 保存商品
     * 扩展spuBo，先保存Spu，再保存SpuDetail(部分参数从spu中获得)
     * 再保存skus，最后保存stock(stock在sku对象中，需要单独获取)
     * 需要开启事务
     * @param spuBo
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveGoods(SpuBo spuBo) {
        //新增spu
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        //新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        saveSkuAndStock(spuBo);

        sendMsg("insert", spuBo.getId());
    }

    /**
     * 保存sku和stock
     * @param spuBo
     */
    private void saveSkuAndStock(SpuBo spuBo) {
        //新增sku
        spuBo.getSkus().forEach(sku -> {
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            //新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });

        sendMsg("insert", spuBo.getId());
    }

    /**
     * 更新商品
     * 因为sku没办法修改，因为以前的sku可能存在也可能消失，所以先删除后添加
     * spu不会消失，直接更新
     * @param spuBo
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(SpuBo spuBo) {
        //查询该spu下的所有sku
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = this.skuMapper.select(record);

        //删除stock
        for (Sku sku : skus) {
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }

        //删除sku
        this.skuMapper.delete(record);

        //更新sku
        //更新stock
        this.saveSkuAndStock(spuBo);


        //更新spu和spuDetail
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMsg("update", spuBo.getId());
    }

    /**
     * 获取spuDetail
     * @param spuID
     * @return
     */
    public SpuDetail querySpuDetailBySpuID(Long spuID) {
        return this.spuDetailMapper.selectByPrimaryKey(spuID);
    }

    /**
     * 根据spuID查询所有sku
     * 需要将库存信息添加上
     * @param spuID
     * @return
     */
    public List<Sku> querySkusBySpuID(Long spuID) {
        Sku record = new Sku();
        record.setSpuId(spuID);
        List<Sku> skus = this.skuMapper.select(record);

        //将库存信息添加
        skus.forEach(sku -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });

        return skus;
    }

    /**
     * 根据id查询spu
     * @param spuId
     * @return
     */
    public Spu querySpuById(Long spuId) {
        Spu spu = this.spuMapper.selectByPrimaryKey(spuId);
        return spu;
    }

    /**
     * producer send the message
     * @param type
     * @param id
     */
    public void sendMsg(String type, Long id) {
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    /**
     * query sku by skuId
     * @param skuId
     * @return
     */
    public Sku querySkuBySkuId(Long skuId) {
        Sku sku = this.skuMapper.selectByPrimaryKey(skuId);
        return sku;
    }
}
