package com.project.hospital.poli.service;

import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.poli.dto.PoliRequest;
import com.project.hospital.poli.dto.PoliResponse;
import com.project.hospital.poli.entity.Poli;
import com.project.hospital.poli.mapper.PoliMapper;
import com.project.hospital.poli.repository.PoliRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoliService {

    private final PoliRepository poliRepository;
    private final PoliMapper poliMapper;

    @Transactional
    public PoliResponse create(PoliRequest request) {
        String name = request.name().trim();

        if (poliRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceConflictException("Poli name already exists");
        }

        Poli poli = poliMapper.toEntity(request);
        poli.setName(name);

        return poliMapper.toResponse(poliRepository.save(poli));
    }

    @Transactional(readOnly = true)
    public List<PoliResponse> getAll() {
        return poliRepository.findAll(Sort.by("name")).stream()
                .map(poliMapper::toResponse)
                .toList();
    }

    @Transactional
    public PoliResponse update(Long id, PoliRequest request) {
        Poli poli = poliRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poli not found"));

        String name = request.name().trim();

        if (poliRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResourceConflictException("Poli name already exists");
        }

        poliMapper.updateEntity(request, poli);
        poli.setName(name);

        return poliMapper.toResponse(poliRepository.save(poli));
    }
}