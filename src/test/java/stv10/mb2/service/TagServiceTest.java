package stv10.mb2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stv10.mb2.model.Tag;
import stv10.mb2.repository.ExpenseRepository;
import stv10.mb2.repository.FixedExpenseRepository;
import stv10.mb2.repository.MonthlyFixedExpenseRepository;
import stv10.mb2.repository.TagRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private FixedExpenseRepository fixedExpenseRepository;

    @Mock
    private MonthlyFixedExpenseRepository monthlyFixedExpenseRepository;

    @InjectMocks
    private TagService tagService;

    private Tag sampleTag;
    private UUID tagId;

    @BeforeEach
    void setUp() {
        tagId = UUID.randomUUID();
        sampleTag = Tag.builder()
                .id(tagId)
                .name("Supermercado")
                .icon("ShoppingCart")
                .build();
    }

    @Test
    void createTag_Success() {
        when(tagRepository.existsByNameIgnoreCase("Supermercado")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(sampleTag);

        Tag created = tagService.createTag(Tag.builder().name(" Supermercado ").icon("ShoppingCart").build());

        assertNotNull(created);
        assertEquals("Supermercado", created.getName());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void createTag_DefaultIcon_WhenBlank() {
        when(tagRepository.existsByNameIgnoreCase("Varios")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tag created = tagService.createTag(Tag.builder().name("Varios").build());

        assertNotNull(created);
        assertEquals("HelpCircle", created.getIcon());
    }

    @Test
    void createTag_ThrowsException_WhenDuplicate() {
        when(tagRepository.existsByNameIgnoreCase("Supermercado")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                tagService.createTag(Tag.builder().name("Supermercado").build())
        );
    }

    @Test
    void deleteTag_UnlinksAndDeletes() {
        when(tagRepository.existsById(tagId)).thenReturn(true);

        tagService.deleteTag(tagId);

        verify(expenseRepository).unlinkTag(tagId);
        verify(fixedExpenseRepository).unlinkTag(tagId);
        verify(monthlyFixedExpenseRepository).unlinkTag(tagId);
        verify(tagRepository).deleteById(tagId);
    }
}
