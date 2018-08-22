package com.biubiu;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Haibiao.Zhang on 2018/8/22.
 */
public class CategoryUtil {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void addCategory(CategoryAddDto categoryAddDto) {
        String name = categoryAddDto.getName();
        if (!StringUtils.isEmpty(name) && duplicateName(name, null)) {
            throw new TradeException(CategoryOperationEnum.DUPLICATED_NAME.getMessage());
        }
        Category category = new Category();
        BeanUtils.copyProperties(categoryAddDto, category);
        int count = categoryMapper.selectCount(null);
        category.setSortNumber(++count);
        category.setEditor(categoryAddDto.getCreator());
        int result = categoryMapper.insertSelective(category);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.ADD_FAIL.getMessage());
        }
    }

    @Override
    public void updateCategory(CategoryUpdateDto categoryUpdateDto) {
        String id = categoryUpdateDto.getId();
        getCategoryById(id);
        String name = categoryUpdateDto.getName();
        if (!StringUtils.isEmpty(name) && duplicateName(categoryUpdateDto.getName(), id)) {
            throw new TradeException(CategoryOperationEnum.DUPLICATED_NAME.getMessage());
        }
        Category update = new Category();
        BeanUtils.copyProperties(categoryUpdateDto, update);
        int result = categoryMapper.updateByPrimaryKeySelective(update);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.UPDATE_FAIL.getMessage());
        }
    }

    @Override
    public void deleteCategory(String id, String editor) {
        Category category = getCategoryById(id);
        LinkedList<CategoryRelationDto> relations = getCategoryRelation();
        //查出所有子类别并删除
        List<Category> children = getCategoryChildrenById(relations, id);
        if (!children.isEmpty()) {
            for (Category c : children) {
                doDeleteCategory(c, editor);
            }
        }
        //删除父类别
        int result = doDeleteCategory(category, editor);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.DELETE_FAIL.getMessage());
        }
    }

    @Override
    public Pager<Category> categories(CategoryListDto categoryListDto) {
        Pager<Category> result = new Pager<>();
        PageInfo<Category> categories = PageHelper.startPage(categoryListDto.getPage(), categoryListDto.getSize())
                .doSelectPageInfo(() -> categoryMapper.categories(categoryListDto));
        result.setContent(categories.getList());
        result.setPage(categories.getPageNum());
        result.setSize(categories.getPageSize());
        result.setTotalPages(categories.getPages());
        result.setTotalElements(categories.getTotal());
        return result;
    }

    @Override
    public void top(CategoryTopDto categoryTopDto) {
        String id = categoryTopDto.getId();
        Category category = getCategoryById(id);
        Date date = DateUtil.now();
        category.setSortType(SortTypeEnum.TOP.getType());
        category.setTopTime(date);
        category.setEditTime(date);
        category.setEditor(categoryTopDto.getEditor());
        category.setBottomTime(null);
        int result = categoryMapper.updateCategoryToTop(category);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.TOP_FAIL.getMessage());
        }
    }

    @Override
    public void bottom(CategoryBottomDto categoryBottomDto) {
        String id = categoryBottomDto.getId();
        Category category = getCategoryById(id);
        Date date = DateUtil.now();
        category.setSortType(SortTypeEnum.BOTTOM.getType());
        category.setBottomTime(date);
        category.setEditTime(date);
        category.setEditor(categoryBottomDto.getEditor());
        category.setTopTime(null);
        int result = categoryMapper.updateCategoryToBottom(category);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.BOTTOM_FAIL.getMessage());
        }
    }

    @Override
    public void move(CategoryMoveDto categoryMoveDto) {
        String fromId = categoryMoveDto.getFromId();
        String toId = categoryMoveDto.getToId();
        Category from = getCategoryById(fromId);
        Category to = getCategoryById(toId);
        doMove(fromId, to);
        doMove(toId, from);
        //因为唯一性约束需要特殊sql
        int result = categoryMapper.updateSortNumber(categoryMoveDto);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.MOVE_FAIL.getMessage());
        }
    }

    private void doMove(String id, Category category) {
        Category copy = new Category();
        copy.setId(id);
        copy.setSortType(category.getSortType());
        copy.setTopTime(category.getTopTime());
        copy.setBottomTime(category.getBottomTime());
        int result = categoryMapper.updateCategorySelective(copy);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.MOVE_FAIL.getMessage());
        }
    }

    private Category getCategoryById(String id) {
        Category category = categoryMapper.selectByPrimaryKey(id);
        if (category == null) {
            throw new TradeException(CategoryOperationEnum.NOT_EXIST.getMessage());
        }
        return category;
    }

    private boolean duplicateName(String name, String id) {
        //不允许同名
        Weekend<Category> weekend = Weekend.of(Category.class);
        if (StringUtils.isEmpty(id)) {
            weekend.weekendCriteria().andEqualTo(Category::getName, name);
        } else {
            weekend.weekendCriteria().andEqualTo(Category::getName, name).andNotEqualTo(Category::getId, id);
        }
        return !categoryMapper.selectByExample(weekend).isEmpty();
    }

    private CategoryRelationDto initCategoryRelationDto(Category category) {
        CategoryRelationDto categoryRelationDto = new CategoryRelationDto();
        BeanUtils.copyProperties(category, categoryRelationDto);
        return categoryRelationDto;
    }

    private LinkedList<CategoryRelationDto> getCategoryRelation() {
        //一次性查出所有类别及类别之间的关联
        Weekend<Category> weekend = Weekend.of(Category.class);
        weekend.weekendCriteria().andEqualTo(Category::getIsDelete, EntityStatusEnum.EXIST.getStatus());
        List<Category> categoryList = categoryMapper.selectByExample(weekend);
        List<CategoryRelationDto> categoryRelationDtoList = categoryList.stream().map(this::initCategoryRelationDto).collect(Collectors.toList());
        return categoryRelationDtoList.stream().filter(c -> c.getParentId() == null)
                .peek(categoryRelation -> categoryRelation.setChildCategory(this.getCategoryChildren(categoryRelation, categoryRelationDtoList)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private LinkedList<CategoryRelationDto> getCategoryChildren(CategoryRelationDto categoryRelation, List<CategoryRelationDto> categoryRelationDtoList) {
        return categoryRelationDtoList.stream()
                .filter(c -> categoryRelation.getId().equals(c.getParentId()))
                .peek(category -> category.setChildCategory(this.getCategoryChildren(category, categoryRelationDtoList)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private List<Category> getCategoryChildrenById(LinkedList<CategoryRelationDto> relations, String id) {
        //层级遍历树节点找到对应id的节点
        CategoryRelationDto target = traverseTree(relations, id);
        //找到该节点下面的所有子类别
        if (target == null) return Collections.emptyList();
        return findChildren(target);
    }

    private CategoryRelationDto traverseTree(LinkedList<CategoryRelationDto> relations, String id) {
        if (relations.isEmpty()) return null;
        ListIterator<CategoryRelationDto> iterator = relations.listIterator();
        while (iterator.hasNext()) {
            CategoryRelationDto categoryRelationDto = iterator.next();
            if (categoryRelationDto.getId().equals(id)) {
                return categoryRelationDto;
            }
            iterator.remove();
            categoryRelationDto.getChildCategory().forEach(iterator::add);
        }
        return null;
    }

    private List<Category> findChildren(CategoryRelationDto target) {
        List<Category> categoryList = new ArrayList<>();
        LinkedList<CategoryRelationDto> children = target.getChildCategory();
        ListIterator<CategoryRelationDto> iterator = children.listIterator();
        while (iterator.hasNext()) {
            CategoryRelationDto categoryRelationDto = iterator.next();
            Category category = new Category();
            BeanUtils.copyProperties(categoryRelationDto, category);
            categoryList.add(category);
            iterator.remove();
        }
        return categoryList;
    }

    private int doDeleteCategory(Category c, String editor) {
        if (c.getAssociatedNumber() > 0) {
            throw new TradeException(CategoryOperationEnum.ASSOCIATED_NUMBER_NOT_ZERO.getMessage());
        }
        c.setIsDelete(EntityStatusEnum.DELETE.getStatus());
        c.setEditor(editor);
        c.setEditTime(DateUtil.now());
        return categoryMapper.updateByPrimaryKeySelective(c);
    }

}
