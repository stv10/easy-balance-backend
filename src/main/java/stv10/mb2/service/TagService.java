package stv10.mb2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stv10.mb2.dto.CreateTagDTO;
import stv10.mb2.dto.UpdateTagDTO;
import stv10.mb2.model.Tag;
import stv10.mb2.repository.ExpenseRepository;
import stv10.mb2.repository.FixedExpenseRepository;
import stv10.mb2.repository.MonthlyFixedExpenseRepository;
import stv10.mb2.repository.TagRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ExpenseRepository expenseRepository;
    private final FixedExpenseRepository fixedExpenseRepository;
    private final MonthlyFixedExpenseRepository monthlyFixedExpenseRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Tag> getTagById(UUID id) {
        return tagRepository.findById(id);
    }

    @Transactional
    public Tag createTag(CreateTagDTO dto) {
        return createTag(Tag.builder()
                .name(dto.name())
                .icon(dto.icon())
                .color(dto.color())
                .build());
    }

    @Transactional
    public Tag updateTag(UUID id, UpdateTagDTO dto) {
        return updateTag(id, Tag.builder()
                .name(dto.name())
                .icon(dto.icon())
                .color(dto.color())
                .build());
    }

    @Transactional
    public Tag createTag(Tag tag) {
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }
        String trimmedName = tag.getName().trim();
        if (tagRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Ya existe una etiqueta con el nombre: " + trimmedName);
        }
        tag.setName(trimmedName);
        if (tag.getIcon() == null || tag.getIcon().trim().isEmpty()) {
            tag.setIcon("HelpCircle");
        } else {
            tag.setIcon(tag.getIcon().trim());
        }
        if (tag.getColor() == null || tag.getColor().trim().isEmpty()) {
            tag.setColor("#3b82f6");
        } else {
            tag.setColor(tag.getColor().trim());
        }
        return tagRepository.save(tag);
    }

    @Transactional
    public Tag updateTag(UUID id, Tag tagDetails) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Etiqueta no encontrada con id: " + id));

        if (tagDetails.getName() == null || tagDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }
        String trimmedName = tagDetails.getName().trim();
        tagRepository.findByNameIgnoreCase(trimmedName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otra etiqueta con el nombre: " + trimmedName);
            }
        });

        tag.setName(trimmedName);
        if (tagDetails.getIcon() == null || tagDetails.getIcon().trim().isEmpty()) {
            tag.setIcon("HelpCircle");
        } else {
            tag.setIcon(tagDetails.getIcon().trim());
        }
        if (tagDetails.getColor() != null && !tagDetails.getColor().trim().isEmpty()) {
            tag.setColor(tagDetails.getColor().trim());
        }
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new IllegalArgumentException("Etiqueta no encontrada con id: " + id);
        }
        expenseRepository.unlinkTag(id);
        fixedExpenseRepository.unlinkTag(id);
        monthlyFixedExpenseRepository.unlinkTag(id);
        tagRepository.deleteById(id);
    }
}
