package com.su.mall.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.UmsMemberReceiveAddressMapper;
import com.su.mall.model.UmsMember;
import com.su.mall.model.UmsMemberReceiveAddress;
import com.su.mall.portal.service.UmsMemberReceiveAddressService;
import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 用户地址管理Service实现类
 *
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsMemberReceiveAddressServiceImpl implements UmsMemberReceiveAddressService {
    private final UmsMemberService memberService;
    private final UmsMemberReceiveAddressMapper addressMapper;
    @Override
    public int add(UmsMemberReceiveAddress address) {
        UmsMember currentMember = memberService.getCurrentMember();
        address.setMemberId(currentMember.getId());
        return addressMapper.insert(address);
    }

    @Override
    public int delete(Long id) {
        UmsMember currentMember = memberService.getCurrentMember();
        return addressMapper.delete(
                new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                        .eq(UmsMemberReceiveAddress::getMemberId, currentMember.getId())
                        .eq(UmsMemberReceiveAddress::getId, id));
    }

    @Override
    public int update(Long id, UmsMemberReceiveAddress address) {
        address.setId(null);
        UmsMember currentMember = memberService.getCurrentMember();
        if(address.getDefaultStatus()==null){
            address.setDefaultStatus(0);
        }
        if(address.getDefaultStatus()==1){
            //先将原来的默认地址去除
            UmsMemberReceiveAddress record= new UmsMemberReceiveAddress();
            record.setDefaultStatus(0);
            addressMapper.update(record,
                    new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                            .eq(UmsMemberReceiveAddress::getMemberId, currentMember.getId())
                            .eq(UmsMemberReceiveAddress::getDefaultStatus, 1));
        }
        return addressMapper.update(address,
                new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                        .eq(UmsMemberReceiveAddress::getMemberId, currentMember.getId())
                        .eq(UmsMemberReceiveAddress::getId, id));
    }

    @Override
    public List<UmsMemberReceiveAddress> list() {
        UmsMember currentMember = memberService.getCurrentMember();
        return addressMapper.selectList(
                new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                        .eq(UmsMemberReceiveAddress::getMemberId, currentMember.getId()));
    }

    @Override
    public UmsMemberReceiveAddress getItem(Long id) {
        UmsMember currentMember = memberService.getCurrentMember();
        List<UmsMemberReceiveAddress> addressList = addressMapper.selectList(
                new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                        .eq(UmsMemberReceiveAddress::getMemberId, currentMember.getId())
                        .eq(UmsMemberReceiveAddress::getId, id));
        if(!CollectionUtils.isEmpty(addressList)){
            return addressList.get(0);
        }
        return null;
    }
}
