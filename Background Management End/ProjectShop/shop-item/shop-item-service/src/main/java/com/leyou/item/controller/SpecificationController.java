package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据cid查询参数分组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecByCid(@PathVariable Long cid) {
        List<SpecGroup> groups = this.specificationService.querySpecByCid(cid);
        if(CollectionUtils.isEmpty(groups)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    /**
     * 添加参数组
     * @param specGroup
     * @return
     */
    @PostMapping("group")
    public ResponseEntity<Void> saveGroup(@RequestBody SpecGroup specGroup) {
        this.specificationService.saveGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 更新添加参数组
     * @param specGroup
     * @return
     */
    @PutMapping("group")
    public ResponseEntity<Void> updateGroup(@RequestBody SpecGroup specGroup) {
        this.specificationService.updateGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据gid删除参数组
     * @param id
     */
    @DeleteMapping("group/{id}")
    public void deleteGroupByGid(@PathVariable Long id) {
        this.specificationService.deleteGroupByGid(id);
    }


    /**
     * 根据cid等请求查询参数列表
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = true) Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching", required = false) Boolean searching
    ) {
        List<SpecParam> params = this.specificationService.queryParams(gid, cid, generic, searching);

        if(CollectionUtils.isEmpty(params)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(params);
    }

    @PostMapping("param")
    public ResponseEntity<Void> saveParam(@RequestBody SpecParam specParam) {
        this.specificationService.saveParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 更新参数
     * @param specParam
     * @return
     */
    @PutMapping("param")
    public ResponseEntity<Void> updateParam(@RequestBody SpecParam specParam) {
        this.specificationService.updateParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除参数
     * @param id
     */
    @DeleteMapping("param/{id}")
    public void deleteParam(@PathVariable Long id) {
        this.specificationService.deleteParam(id);
    }


    /**
     * 根据id查询参数组，并查询组下相应的规格参数
     * @param cid
     * @return
     */
    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupsWithParams(@PathVariable("cid") Long cid) {
        List<SpecGroup> groups = this.specificationService.querySpecGroupsWithParams(cid);

        if(CollectionUtils.isEmpty(groups)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }
}
