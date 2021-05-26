package com.decagon.android.sq007.room

import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.util.EntityMapper

class CachedPostMapper : EntityMapper<PostEntity, Post> {
    override fun mapFromEntity(entity: PostEntity): Post {
        return Post(
            userId = entity.userId,
            id = entity.id,
            title = entity.title,
            body = entity.body
        )
    }

    override fun mapToEntity(domainModel: Post): PostEntity {
        return PostEntity(
            userId = domainModel.userId,
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body
        )
    }

    fun mapFromEntityList(entities: List<PostEntity>): List<Post> {
        return entities.map { mapFromEntity(it) }
    }

    fun mapToEntityList(domainModel: List<Post>): List<PostEntity> {
        return domainModel.map { mapToEntity(it) }
    }
}