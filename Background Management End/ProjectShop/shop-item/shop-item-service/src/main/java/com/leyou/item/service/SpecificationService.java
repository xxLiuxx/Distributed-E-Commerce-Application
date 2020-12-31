package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据cid查询参数组
     * @param cid
     * @return
     */
    public List<SpecGroup> querySpecByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return this.specGroupMapper.select(specGroup);
    }

    /**
     * 新增参数组
     * @param specGroup
     */
    public void saveGroup(SpecGroup specGroup) {
        this.specGroupMapper.insertSelective(specGroup);
    }

    /**
     * 更新参数组
     * @param specGroup
     */
    public void updateGroup (SpecGroup specGroup) {
        this.specGroupMapper.updateByPrimaryKey(specGroup);
    }

    /**
     * 根据id删除参数组
     * @param id
     */
    public void deleteGroupByGid(Long id) {
        this.specGroupMapper.deleteByPrimaryKey(id);
    }


    /**
     * 新增参数
     * @param specParam
     */
    public void saveParam(SpecParam specParam) {
        this.specParamMapper.insertSelective(specParam);
    }

    /**
     * 更新参数
     * @param specParam
     */
    public void updateParam(SpecParam specParam) {
        this.specParamMapper.updateByPrimaryKey(specParam);
    }

    /**
     * 删除参数
     * @param id
     */
    public void deleteParam(Long id) {
        this.specParamMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据cid等请求查询参数列表
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        return this.specParamMapper.select(specParam);
    }

    /**
     * 根据id查询参数组，并查询组下相应的规格参数
     * @param cid
     * @return
     */
    public List<SpecGroup> querySpecGroupsWithParams(Long cid) {
        List<SpecGroup> groups = this.querySpecByCid(cid);
        groups.forEach(group -> {
            List<SpecParam> params = this.queryParams(group.getId(), null, null, null);
            group.setParams(params);
        });
        return groups;
    }
}
