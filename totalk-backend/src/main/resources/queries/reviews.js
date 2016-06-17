var findReviewsByEntity = function(entityId, entityType, type, limit, offset){
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
                                    "entityType":entityType
                                }
                            },
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