package com.mylogo.visitors.convertor;

import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VisitorConverter {

    public static VisitorDTO toDTO(Visitor visitor) {
        VisitorDTO dto = new VisitorDTO();
        BeanUtils.copyProperties(visitor, dto);
        return dto;
    }

    public static List<VisitorDTO> toDTOList(List<Visitor> visitors) {
        return visitors.stream().map(VisitorConverter::toDTO).collect(Collectors.toList());
    }
}
