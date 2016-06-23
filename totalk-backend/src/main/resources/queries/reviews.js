var findReviewsByEntity = function(entityId, type, limit, offset){
    return {
        "size": limit,
        "from": offset,
        "sort": { "createdAt" : {"order" : "desc"}},
        "_source": false,
        "query": {
            "filtered": {
                "filter": {
                    "bool":{
                        "must": [
                            {
                                "term": {
                                    "entityId":entityId
                                }
                            },
                            {
                                "term": {
                                    "type":type
                                }
                            }
                        ],
                        "must_not": [
                            {
                                "term":{
                                    "deleted": true
                                }
                            }
                        ]
                    }
                }
            }
        }
    }
}

var commentsByReviewIdQuery = function(reviewIds){
    return {
        "filtered": {
            "filter": {
                "bool":{
                    "must": [
                        {
                            "terms": {
                                "reviewId":reviewIds.constructor === Array ? reviewIds : [reviewIds]
                            }
                        }
                    ],
                    "must_not": [
                        {
                            "term":{
                                "deleted": true
                            }
                        }
                    ]
                }
            }
        }
    }
}

var findCommentsByReview = function(reviewId, limit, offset){
    return {
        "size": limit,
        "from": offset,
        "sort": { "createdAt" : {"order" : "desc"}},
        "_source": false,
        "query": commentsByReviewIdQuery(reviewId)
    }
}

var getCommentsCount = function(reviewIds){
    return {
        "size": 0,
        "_source": false,
        "query": commentsByReviewIdQuery(reviewIds),
        "aggs":{
            "comments": {
                "terms" : { "field" : "reviewId","size" : 0 }
            }
        }
    }
}

var getReviewsCount = function(entityIds){
    return {
        "size": 0,
        "_source": false,
        "query":{
            "filtered": {
                "filter": {
                    "bool":{
                        "must": [
                            {
                                "terms": {
                                    "entityId":entityIds.constructor === Array ? entityIds : [entityIds]
                                }
                            }
                        ],
                        "must_not": [
                            {
                                "term":{
                                    "deleted": true
                                }
                            }
                        ]
                    }
                }
            }
        },
        "aggs": {
            "entities": {
                "terms" : {
                    "field" : "entityId",
                    "size" : 0
                },
                "aggs": {
                    "reviewTypes": {
                        "terms" : {
                            "field" : "type",
                            "size" : 0
                        }
                    }
                }
            }
        }
    }
}
