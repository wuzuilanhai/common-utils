/**类别表*/
CREATE TABLE t_category (
  id VARCHAR2(24) NOT NULL,
  name VARCHAR2(24),
  status number(2) default 1,
  parent_id VARCHAR2(24),
  minimum number(1) default 0,
  associated_number    number(1) default 0,
  sort_type            number(2) default 1,
  sort_number          number(6),
  top_time             timestamp,
  bottom_time          timestamp,
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  CONSTRAINT constraint_sort UNIQUE (sort_number),
  PRIMARY KEY (id)
);
comment on table t_category is '类别表';
comment on column t_category.name is '类别名称';
comment on column t_category.status is '类别状态 0-禁用 1-启用 ';
comment on column t_category.parent_id is '父类别ID';
comment on column t_category.minimum is '是否最小分类 0-否 1-是';
comment on column t_category.associated_number is '关联商品数';
comment on column t_category.sort_type is '类型 2-置顶 1-普通 0-置底';
comment on column t_category.sort_number  is '排序号';
comment on column t_category.top_time is '置顶时间';
comment on column t_category.bottom_time is '置底时间';
comment on column t_category.create_time is '创建时间';
comment on column t_category.creator is '创建人';
comment on column t_category.edit_time is '修改时间';
comment on column t_category.editor is '修改人';
comment on column t_category.is_delete is '是否删除 0-未删除 1-已删除';

/**商品表*/
CREATE TABLE t_product (
  id VARCHAR2(24) NOT NULL,
  product_code VARCHAR2(64),
  name VARCHAR2(64),
  category_id VARCHAR2(24),
  detail VARCHAR2(2550),
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  PRIMARY KEY (id)
);
comment on table t_product is '商品表';
comment on column t_product.product_code is '商品编码';
comment on column t_product.name is '商品名称';
comment on column t_product.category_id is '类别ID';
comment on column t_product.detail is '商品详情';
comment on column t_product.create_time is '创建时间';
comment on column t_product.creator is '创建人';
comment on column t_product.edit_time is '修改时间';
comment on column t_product.editor is '修改人';
comment on column t_product.is_delete is '是否删除 0-未删除 1-已删除';

/**商品图片表*/
CREATE TABLE t_images (
  id VARCHAR2(24) NOT NULL,
  product_id VARCHAR2(24),
  url VARCHAR2(255),
  master               number(1) default 0,
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  PRIMARY KEY (id)
);
comment on table t_images is '商品图片表';
comment on column t_images.product_id is '商品id';
comment on column t_images.url is '图片url';
comment on column t_images.master is '是否主图 0-不是 1-是';
comment on column t_images.create_time is '创建时间';
comment on column t_images.creator is '创建人';
comment on column t_images.edit_time is '修改时间';
comment on column t_images.editor is '修改人';
comment on column t_images.is_delete is '是否删除 0-未删除 1-已删除';

/**商品通用属性表*/
CREATE TABLE t_common_attr (
  id VARCHAR2(24) NOT NULL,
  name VARCHAR2(64) not null,
  description VARCHAR2(2048),
  edit_type number(1) not null,
  value_number         number(4) default 0,
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  PRIMARY KEY (id)
);
comment on table t_common_attr is '商品通用属性表';
comment on column t_common_attr.name is '属性名称';
comment on column t_common_attr.description is '属性描述';
comment on column t_common_attr.edit_type is '编辑方式 1-单选 2-多选';
comment on column t_common_attr.value_number is '属性值个数';
comment on column t_common_attr.create_time is '创建时间';
comment on column t_common_attr.creator is '创建人';
comment on column t_common_attr.edit_time is '修改时间';
comment on column t_common_attr.editor is '修改人';
comment on column t_common_attr.is_delete is '是否删除 0-未删除 1-已删除';

/**商品通用属性值表*/
CREATE TABLE t_common_value (
  id VARCHAR2(24) NOT NULL,
  common_attr_id VARCHAR2(24) NOT NULL,
  name VARCHAR2(64) not null,
  sort number(4),
  sort_type            number(2) default 1,
  sort_number          number(6),
  top_time             timestamp,
  bottom_time          timestamp,
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  CONSTRAINT common_value_sort UNIQUE (sort_number),
  PRIMARY KEY (id)
);
comment on table t_common_value is '商品通用属性值表';
comment on column t_common_value.common_attr_id is '商品通用属性id';
comment on column t_common_value.name is '商品属性值名称';
comment on column t_common_value.sort is '排序';
comment on column t_common_value.sort_type is '类型 2-置顶 1-普通 0-置底';
comment on column t_common_value.sort_number  is '排序号';
comment on column t_common_value.top_time is '置顶时间';
comment on column t_common_value.bottom_time is '置底时间';
comment on column t_common_value.create_time is '创建时间';
comment on column t_common_value.creator is '创建人';
comment on column t_common_value.edit_time is '修改时间';
comment on column t_common_value.editor is '修改人';
comment on column t_common_value.is_delete is '是否删除 0-未删除 1-已删除';

/**商品属性表*/
CREATE TABLE t_attr (
  id VARCHAR2(24) NOT NULL,
  category_id VARCHAR2(24),
  common_attr_id VARCHAR2(24),
  name VARCHAR2(64) not null,
  description VARCHAR2(2048),
  edit_type number(1) not null,
  type                 number(1) not null,
  required             number(1) not null,
  value_number         number(4) default 0,
  sort_type            number(2) default 1,
  sort_number          number(6),
  top_time             timestamp,
  bottom_time          timestamp,
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  CONSTRAINT attr_sort UNIQUE (sort_number),
  PRIMARY KEY (id)
);
comment on table t_attr is '商品属性表';
comment on column t_attr.category_id is '类别ID';
comment on column t_attr.common_attr_id is '通用属性ID';
comment on column t_attr.name is '属性名称';
comment on column t_attr.description is '属性描述';
comment on column t_attr.edit_type is '编辑方式 1-单选 2-多选';
comment on column t_attr.type is '属性类型 1-通用属性 2-自定义属性';
comment on column t_attr.required is '是否必填 0-不是 1-是';
comment on column t_attr.value_number is '属性值个数';
comment on column t_attr.sort_type is '类型 2-置顶 1-普通 0-置底';
comment on column t_attr.sort_number  is '排序号';
comment on column t_attr.top_time is '置顶时间';
comment on column t_attr.bottom_time is '置底时间';
comment on column t_attr.create_time is '创建时间';
comment on column t_attr.creator is '创建人';
comment on column t_attr.edit_time is '修改时间';
comment on column t_attr.editor is '修改人';
comment on column t_attr.is_delete is '是否删除 0-未删除 1-已删除';

/**商品属性值表*/
CREATE TABLE t_value (
  id VARCHAR2(24) NOT NULL,
  attr_id VARCHAR2(24) NOT NULL,
  name VARCHAR2(64) not null,
  sort number(4),
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  PRIMARY KEY (id)
);
comment on table t_value is '商品属性值表';
comment on column t_value.attr_id is '商品属性id';
comment on column t_value.name is '商品属性值名称';
comment on column t_value.sort is '排序';
comment on column t_value.create_time is '创建时间';
comment on column t_value.creator is '创建人';
comment on column t_value.edit_time is '修改时间';
comment on column t_value.editor is '修改人';
comment on column t_value.is_delete is '是否删除 0-未删除 1-已删除';

/**商品属性关联表*/
CREATE TABLE t_product_attr (
  id VARCHAR2(24) NOT NULL,
  product_id VARCHAR2(24) NOT NULL,
  attr_id VARCHAR2(24) NOT NULL,
  create_time          timestamp default systimestamp,
  creator              VARCHAR2(64),
  edit_time            timestamp default systimestamp,
  editor               VARCHAR2(64),
  is_delete            number(1) default 0,
  PRIMARY KEY (id)
);
comment on table t_product_attr is '商品属性关联表';
comment on column t_product_attr.product_id is '商品id';
comment on column t_product_attr.attr_id is '商品属性id';
comment on column t_product_attr.create_time is '创建时间';
comment on column t_product_attr.creator is '创建人';
comment on column t_product_attr.edit_time is '修改时间';
comment on column t_product_attr.editor is '修改人';
comment on column t_product_attr.is_delete is '是否删除 0-未删除 1-已删除';