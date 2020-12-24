package com.github.paulosalonso.research.application.controller;

import com.github.paulosalonso.research.application.dto.OptionCriteriaDTO;
import com.github.paulosalonso.research.application.dto.OptionDTO;
import com.github.paulosalonso.research.application.dto.OptionInputDTO;
import com.github.paulosalonso.research.application.mapper.OptionDTOMapper;
import com.github.paulosalonso.research.usecase.exception.NotFoundException;
import com.github.paulosalonso.research.usecase.option.OptionCreate;
import com.github.paulosalonso.research.usecase.option.OptionDelete;
import com.github.paulosalonso.research.usecase.option.OptionRead;
import com.github.paulosalonso.research.usecase.option.OptionUpdate;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Api(tags = "Options")
@RequiredArgsConstructor
@RestController
@RequestMapping("/questions/{questionId}/options")
public class OptionController {

    private final OptionCreate optionCreate;
    private final OptionRead optionRead;
    private final OptionUpdate optionUpdate;
    private final OptionDelete optionDelete;
    private final OptionDTOMapper mapper;

    @GetMapping("/{optionId}")
    public OptionDTO get(@PathVariable UUID questionId, @PathVariable UUID optionId) {
        return mapper.toDTO(optionRead.read(questionId, optionId));
    }

    @GetMapping
    public List<OptionDTO> search(@PathVariable UUID questionId, OptionCriteriaDTO criteria) {
        return optionRead.search(questionId, mapper.toDomain(criteria)).stream()
                .map(mapper::toDTO)
                .collect(toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OptionDTO create(@PathVariable UUID questionId, @RequestBody @Valid OptionInputDTO optionInputDTO) {
        try {
            var created = optionCreate.create(questionId, mapper.toDomain(optionInputDTO));
            return mapper.toDTO(created);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question not found: " + questionId);
        }
    }

    @PutMapping("/{optionId}")
    public OptionDTO update(@PathVariable UUID questionId, @PathVariable UUID optionId,
                              @RequestBody @Valid OptionInputDTO optionInputDTO) {

        var question = mapper.toDomain(optionInputDTO).toBuilder()
                .id(optionId)
                .build();

        var updated = optionUpdate.update(questionId, question);
        return mapper.toDTO(updated);
    }

    @DeleteMapping("/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID questionId, @PathVariable UUID optionId) {
        optionDelete.delete(questionId, optionId);
    }

}