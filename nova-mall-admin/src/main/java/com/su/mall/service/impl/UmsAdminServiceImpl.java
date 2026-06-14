package com.su.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.bo.AdminUserDetails;
import com.su.mall.common.exception.Asserts;
import com.su.mall.common.util.RequestUtil;
import com.su.mall.dao.UmsAdminRoleRelationDao;
import com.su.mall.dto.UmsAdminParam;
import com.su.mall.dto.UpdateAdminPasswordParam;
import com.su.mall.mapper.UmsAdminLoginLogMapper;
import com.su.mall.mapper.UmsAdminMapper;
import com.su.mall.mapper.UmsAdminRoleRelationMapper;
import com.su.mall.model.*;
import com.su.mall.security.util.JwtTokenUtil;
import com.su.mall.security.util.SpringUtil;
import com.su.mall.service.UmsAdminCacheService;
import com.su.mall.service.UmsAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 后台用户管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsAdminServiceImpl implements UmsAdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final UmsAdminMapper adminMapper;
    private final UmsAdminRoleRelationMapper adminRoleRelationMapper;
    private final UmsAdminRoleRelationDao adminRoleRelationDao;
    private final UmsAdminLoginLogMapper loginLogMapper;

    @Override
    public UmsAdmin getAdminByUsername(String username) {
        //先从缓存中获取数据
        UmsAdmin admin = getCacheService().getAdmin(username);
        if (admin != null) return admin;
        //缓存中没有从数据库中获取
        // ✅ 改造：LambdaQueryWrapper 替代 Example
        UmsAdmin adminDB = adminMapper.selectOne(
            new LambdaQueryWrapper<UmsAdmin>().eq(UmsAdmin::getUsername, username)
        );
        if (adminDB != null) {
            //将数据库中的数据存入缓存中
            getCacheService().setAdmin(adminDB);
            return adminDB;
        }
        return null;
    }

    @Override
    @Transactional
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        //查询是否有相同用户名的用户
        // ✅ 改造：LambdaQueryWrapper 替代 Example
        UmsAdmin existAdmin = adminMapper.selectOne(
            new LambdaQueryWrapper<UmsAdmin>().eq(UmsAdmin::getUsername, umsAdmin.getUsername())
        );
        if (existAdmin != null) {
            return null;
        }
        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        // ✅ 改造：insert 替代 insertSelective
        adminMapper.insert(umsAdmin);
        umsAdmin.setPassword(null);
        return umsAdmin;
    }

    @Override
    @Transactional
    public String login(String username, String password) {
        String token = null;
        //密码需要客户端加密后传递
        try {
            UserDetails userDetails = loadUserByUsername(username);
            if(!passwordEncoder.matches(password,userDetails.getPassword())){
                Asserts.fail("密码不正确");
            }
            if(!userDetails.isEnabled()){
                Asserts.fail("帐号已被禁用");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
//            updateLoginTimeByUsername(username);
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }

    /**
     * 添加登录记录
     * @param username 用户名
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        if(admin==null) return;
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            loginLog.setIp(RequestUtil.getRequestIp(request));
        }
        // ✅ 改造：insert 替代 insert
        loginLogMapper.insert(loginLog);
    }

    /**
     * 根据用户名修改登录时间
     *
     * @author Su
     */
    private void updateLoginTimeByUsername(String username) {
        // ✅ 改造：使用 update + LambdaUpdateWrapper 替代 updateByExampleSelective
        adminMapper.update(null,
            new LambdaUpdateWrapper<UmsAdmin>()
                .set(UmsAdmin::getLoginTime, new Date())
                .eq(UmsAdmin::getUsername, username)
        );
    }

    @Override
    public String refreshToken(String oldToken) {
        return jwtTokenUtil.refreshHeadToken(oldToken);
    }

    @Override
    public UmsAdmin getItem(Long id) {
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        return adminMapper.selectById(id);
    }

    @Override
    public Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        // ✅ 改造：使用 MyBatis-Plus 分页
        LambdaQueryWrapper<UmsAdmin> wrapper = new LambdaQueryWrapper<>();
        if (!StrUtil.isEmpty(keyword)) {
            wrapper.like(UmsAdmin::getUsername, keyword)
                .or()
                .like(UmsAdmin::getNickName, keyword);
        }
        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        return adminMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public int update(Long id, UmsAdmin admin) {
        admin.setId(id);
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        UmsAdmin rawAdmin = adminMapper.selectById(id);
        if(rawAdmin == null){
            return 0;
        }
        if(rawAdmin.getPassword() != null && rawAdmin.getPassword().equals(admin.getPassword())){
            //与原加密密码相同的不需要修改
            admin.setPassword(null);
        }else{
            //与原加密密码不同的需要加密修改
            if(StrUtil.isEmpty(admin.getPassword())){
                admin.setPassword(null);
            }else{
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
        }
        // ✅ 改造：updateById 替代 updateByPrimaryKeySelective
        int count = adminMapper.updateById(admin);
        getCacheService().delAdmin(id);
        return count;
    }

    @Override
    @Transactional
    public int delete(Long id) {
        // ✅ 改造：deleteById 替代 deleteByPrimaryKey
        int count = adminMapper.deleteById(id);
        getCacheService().delAdmin(id);
        getCacheService().delResourceList(id);
        return count;
    }

    @Override
    @Transactional
    public int updateRole(Long adminId, List<Long> roleIds) {
        int count = roleIds == null ? 0 : roleIds.size();
        //先删除原来的关系
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        adminRoleRelationMapper.delete(
            new LambdaQueryWrapper<UmsAdminRoleRelation>().eq(UmsAdminRoleRelation::getAdminId, adminId)
        );
        //建立新关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UmsAdminRoleRelation> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                UmsAdminRoleRelation roleRelation = new UmsAdminRoleRelation();
                roleRelation.setAdminId(adminId);
                roleRelation.setRoleId(roleId);
                list.add(roleRelation);
            }
            adminRoleRelationDao.insertList(list);
        }
        getCacheService().delResourceList(adminId);
        return count;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return adminRoleRelationDao.getRoleList(adminId);
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        //先从缓存中获取数据
        List<UmsResource> resourceList = getCacheService().getResourceList(adminId);
        if(CollUtil.isNotEmpty(resourceList)){
            return  resourceList;
        }
        //缓存中没有从数据库中获取
        resourceList = adminRoleRelationDao.getResourceList(adminId);
        if(CollUtil.isNotEmpty(resourceList)){
            //将数据库中的数据存入缓存中
            getCacheService().setResourceList(adminId,resourceList);
        }
        return resourceList;
    }

    @Override
    @Transactional
    public int updatePassword(UpdateAdminPasswordParam param) {
        if(StrUtil.isEmpty(param.getUsername())
                ||StrUtil.isEmpty(param.getOldPassword())
                ||StrUtil.isEmpty(param.getNewPassword())){
            return -1;
        }
        // ✅ 改造：LambdaQueryWrapper 替代 Example
        UmsAdmin umsAdmin = adminMapper.selectOne(
            new LambdaQueryWrapper<UmsAdmin>().eq(UmsAdmin::getUsername, param.getUsername())
        );
        if(umsAdmin == null){
            return -2;
        }
        if(!passwordEncoder.matches(param.getOldPassword(),umsAdmin.getPassword())){
            return -3;
        }
        umsAdmin.setPassword(passwordEncoder.encode(param.getNewPassword()));
        // ✅ 改造：updateById 替代 updateByPrimaryKey
        adminMapper.updateById(umsAdmin);
        getCacheService().delAdmin(umsAdmin.getId());
        return 1;
    }

    @Override
    public UserDetails loadUserByUsername(String username){
        //获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            List<UmsResource> resourceList = getResourceList(admin.getId());
            return new AdminUserDetails(admin,resourceList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    @Override
    public UmsAdminCacheService getCacheService() {
        return SpringUtil.getBean(UmsAdminCacheService.class);
    }

    @Override
    public void logout(String username) {
        //清空缓存中的用户相关数据
        UmsAdmin admin = getCacheService().getAdmin(username);
        if (admin != null) {
            getCacheService().delAdmin(admin.getId());
            getCacheService().delResourceList(admin.getId());
        }
    }
}
