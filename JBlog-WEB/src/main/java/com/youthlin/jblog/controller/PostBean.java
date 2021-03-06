package com.youthlin.jblog.controller;

import com.youthlin.jblog.constant.Constant;
import com.youthlin.jblog.dao.CategoryDao;
import com.youthlin.jblog.dao.PostDao;
import com.youthlin.jblog.model.Category;
import com.youthlin.jblog.model.Post;
import com.youthlin.jblog.model.User;
import com.youthlin.jblog.util.EJBUtil;
import com.youthlin.jblog.util.HTTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by lin on 2016-09-07-007.
 * 管理界面 文章
 */
@ManagedBean
@SessionScoped
public class PostBean {
    private static final String ALL_POST = "articles";
    private static final String WRITE = "write";
    private static PostDao postDao = EJBUtil.getBean(PostDao.class);
    private static CategoryDao categoryDao = EJBUtil.getBean(CategoryDao.class);
    private static final Logger log = LoggerFactory.getLogger(PostBean.class);

    private String title;
    private String content;
    private Long categoryId;
    private Boolean allowComment = true;
    private Post newestText;
    private Post newestImage;
    private List<Post> allTextPost = new ArrayList<>();
    private Long id = 0L;

    public PostBean() {
        clear();
        update();
    }

    private void update() {
        log.trace("获取最新POST");
        newestText = postDao.getNewestText();
        newestImage = postDao.getNewestImage();
        allTextPost.clear();
    }

    private void clear() {
        title = "";
        content = "";
        categoryId = 0L;
        id = 0L;
        allowComment = true;
    }

    public String post(boolean publish) {
        Post post = new Post();
        post.setType(Constant.POST_TYPE_TEXT);
        post.setTitle(title);
        post.setContent(content);
        post.setPublishDate(new Date());
        post.setAllowComment(allowComment);
        if (publish) {
            post.setStatus(Constant.POST_PUBLISH);
        } else {
            post.setStatus(Constant.POST_DRAFT);
        }
        User admin = (User) HTTPUtil.getSession().getAttribute(Constant.CURRENT_USER);
        post.setAuthor(admin);
        Category category = categoryDao.find(Category.class, categoryId);
        post.setCategory(category);
        category.setPostCount(category.getPostCount() + 1);
        postDao.save(post);
        categoryDao.update(category);
        if (publish) {
            log.debug("发表文章成功");
        } else {
            log.debug("保存草稿成功");
        }
        HTTPUtil.getSession().setAttribute(Constant.allTextPostListShouldBeUpdated, true);
        clear();
        update();
        log.trace("分类下文章数目有变化，通知分类列表应该更新");
        HTTPUtil.getSession().setAttribute(Constant.textCategoryListShouldBeUpdated, true);
        return ALL_POST;
    }

    public String toUpdatePostPage() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        id = Long.valueOf(request.getParameter("id"));
        Post post = postDao.find(Post.class, id);
        if (post != null) {
            title = post.getTitle();
            content = post.getContent();
            categoryId = post.getCategory().getId();
            allowComment = post.getAllowComment();
        }
        return WRITE;
    }

    public String update(boolean publish) {
        Post post = postDao.find(Post.class, id);
        post.setTitle(title);
        post.setContent(content);
        post.setAllowComment(allowComment);
        post.setPublishDate(new Date());
        if (publish) {
            post.setStatus(Constant.POST_PUBLISH);
        } else {
            post.setStatus(Constant.POST_DRAFT);
        }
        if (!Objects.equals(categoryId, post.getCategory().getId())) {
            Category old = categoryDao.find(Category.class, post.getCategory().getId());
            Category category = categoryDao.find(Category.class, categoryId);
            post.setCategory(category);
            old.setPostCount(old.getPostCount() - 1);
            category.setPostCount(category.getPostCount() + 1);
            categoryDao.update(old);
            categoryDao.update(category);
            log.trace("分类下文章数目有变化，通知分类列表应该更新");
            HTTPUtil.getSession().setAttribute(Constant.textCategoryListShouldBeUpdated, true);
        }
        postDao.update(post);
        clear();
        update();
        if (publish) {
            log.debug("更新文章成功");
        } else {
            log.debug("存为草稿成功");
        }
        return ALL_POST;
    }

    public String delete() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        id = Long.valueOf(request.getParameter("id"));
        Post post = postDao.find(Post.class, id);
        if (post != null) {
            Category c = post.getCategory();
            post.setStatus(Constant.POST_DELETED);
            c.setPostCount(c.getPostCount() - 1);
            categoryDao.update(c);
            postDao.update(post);
            update();
            log.trace("删除文章成功\n分类里文章数目有变化，通知分类列表应该更新");
            HTTPUtil.getSession().setAttribute(Constant.textCategoryListShouldBeUpdated, true);
        }
        return ALL_POST;
    }

    public List<Post> getAllTextPost() {
        if (allTextPost.size() == 0 || Boolean.TRUE.equals(HTTPUtil.getSession().getAttribute(Constant.allTextPostListShouldBeUpdated))) {
            log.trace("获取最新文章列表");
            allTextPost = postDao.getByType(Constant.POST_TYPE_TEXT);
            HTTPUtil.getSession().setAttribute(Constant.allTextPostListShouldBeUpdated, false);
        }
        return allTextPost;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getAllowComment() {
        return allowComment;
    }

    public void setAllowComment(Boolean allowComment) {
        this.allowComment = allowComment;
    }

    public Post getNewestText() {
        return newestText;
    }

    public void setNewestText(Post newestText) {
        this.newestText = newestText;
    }

    public Post getNewestImage() {
        return newestImage;
    }

    public void setNewestImage(Post newestImage) {
        this.newestImage = newestImage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
