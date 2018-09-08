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

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ImagesMapper imagesMapper;

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
        if (!StringUtils.isEmpty(name) && duplicateName(name, id)) {
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
        Date date = DateUtil.now();
        if (!children.isEmpty()) {
            for (Category c : children) {
                doDeleteCategory(c, editor, date);
            }
        }
        //删除父类别
        int result = doDeleteCategory(category, editor, date);
        if (result == 0) {
            throw new TradeException(CategoryOperationEnum.DELETE_FAIL.getMessage());
        }
        //删除关联的通用属性
        deleteAttr(id, editor, date);
    }

    @Override
    public Pager<CategoryDto> categories(CategoryListDto categoryListDto) {
        PageInfo<Category> categories = PageHelper.startPage(categoryListDto.getPage(), categoryListDto.getSize())
                .doSelectPageInfo(() -> categoryMapper.categories(categoryListDto));
        List<CategoryDto> categoryDtoList = ObjectConverterUtil.convertList(categories.getList(), CategoryDto.class);
        Pager<CategoryDto> result = new Pager<>();
        result.setContent(categoryDtoList);
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

    @Override
    public List<CategoryAndProductDto> categoryAndProduct() {
        LinkedList<CategoryRelationDto> relations = getCategoryRelation();
        return initCategoryAndProduct(relations);
    }

    private List<CategoryAndProductDto> initCategoryAndProduct(LinkedList<CategoryRelationDto> relations) {
        List<CategoryAndProductDto> result = new ArrayList<>();
        relations.forEach(relation -> {
            String categoryId = relation.getId();
            CategoryAndProductDto categoryAndProductDto = new CategoryAndProductDto();
            categoryAndProductDto.setCategoryId(categoryId);
            categoryAndProductDto.setCategoryName(relation.getName());
            List<Product> products = getProductByCategoryId(categoryId);
            List<ProductNameDto> productNameDtoList = ObjectConverterUtil.convertList(products, ProductNameDto.class);

            productNameDtoList.forEach(product -> {
                Weekend<Images> imagesWeekend = Weekend.of(Images.class);
                imagesWeekend.weekendCriteria().andEqualTo(Images::getProductId, product.getId())
                        .andEqualTo(Images::getIsDelete, EntityStatusEnum.EXIST.getStatus());
                List<Images> images = imagesMapper.selectByExample(imagesWeekend);
                if (images != null && !images.isEmpty()) {
                    Images image = images.get(0);
                    product.setImageUrl(image.getUrl());
                }
            });

            categoryAndProductDto.setProductNameDtoList(productNameDtoList);
            categoryAndProductDto.setCategoryAndProductDtoList(initCategoryAndProduct(relation.getChildCategory()));
            result.add(categoryAndProductDto);
        });
        return result;
    }

    private List<Product> getProductByCategoryId(String categoryId) {
        Weekend<Product> productWeekend = Weekend.of(Product.class);
        productWeekend.weekendCriteria().andEqualTo(Product::getCategoryId, categoryId)
                .andEqualTo(Product::getIsDelete, EntityStatusEnum.EXIST.getStatus());
        return productMapper.selectByExample(productWeekend);
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

    private int doDeleteCategory(Category c, String editor, Date editTime) {
        if (c.getAssociatedNumber() > 0) {
            throw new TradeException(CategoryOperationEnum.ASSOCIATED_NUMBER_NOT_ZERO.getMessage());
        }
        c.setIsDelete(EntityStatusEnum.DELETE.getStatus());
        c.setEditor(editor);
        c.setEditTime(editTime);
        return categoryMapper.updateByPrimaryKeySelective(c);
    }

    private void deleteAttr(String id, String editor, Date editTime) {
        Weekend<Attr> attrWeekend = Weekend.of(Attr.class);
        attrWeekend.weekendCriteria().andEqualTo(Attr::getCategoryId, id)
                .andEqualTo(Attr::getIsDelete, EntityStatusEnum.EXIST.getStatus());
        Attr attr = new Attr();
        attr.setIsDelete(EntityStatusEnum.DELETE.getStatus());
        attr.setEditor(editor);
        attr.setEditTime(editTime);
        attrMapper.updateByExampleSelective(attr, attrWeekend);
    }

}
