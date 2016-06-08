/**
 * Created by rshamyan on 3/24/16.
 */

var knock = knock || {};
knock.thrift = knock.thrift || {};

knock.thrift.TKnockTransport = function(wsUrl, xhrUrl) {
    this.xhrUrl_ = xhrUrl;
    this.wsUrl_ = wsUrl;
    this.send_buf = null;
    this.recv_buf = '';
    this.wsReset_(this.wsUrl_);
    this.xhrOnly_ = false;
    window.tknock = this;
};

knock.thrift.TKnockTransport.prototype.fnameToArgs = function (fname) {
    return new (eval('totalk.thrift.' + fname.split(":")[0] + "_" + fname.split(":")[1] + "_args"))();
};

knock.thrift.TKnockTransport.prototype.fnameToResult = function (fname, result) {
    return new (eval('totalk.thrift.' + fname.split(":")[0] + "_" + fname.split(":")[1] + "_result"))(result);
};

knock.thrift.TKnockTransport.prototype.open = function() {
    if (this.wsSocket_ && this.wsSocket_.readyState != this.wsSocket_.CLOSED || !this.wsUrl_) {
        //todo
    } else if (!this.xhrOnly_) {
        this.createSocket_();
    }
};

knock.thrift.TKnockTransport.prototype.xhrOnly = function() {
    this.xhrOnly_ = true;
};

knock.thrift.TKnockTransport.prototype.close = function() {
    if (!this.xhrOnly_) {
        this.wsSocket_.onclose = null;
        this.wsSocket_.close();
        this.wsReset_(this.wsUrl_);
    }
};

knock.thrift.TKnockTransport.prototype.read = function(len) {
    throw Error('Shouldn\'t be used in knock-web');
};

knock.thrift.TKnockTransport.prototype.readAll = function() {
    return this.recv_buf;
};

knock.thrift.TKnockTransport.prototype.write = function(buf) {
    this.send_buf = buf;
};

knock.thrift.TKnockTransport.prototype.flush = function () {
    if (!this.xhrOnly_ && this.isWsOpen_()) {
        this.wsSocket_.send(this.send_buf);
    } else {
        var xreq = this.getXmlHttpRequestObject(),
            that = this;

        if (xreq.overrideMimeType) {
            xreq.overrideMimeType('application/x-thrift');
        }

        xreq.onload = function() {
            if (xreq.readyState == 4) {
                that.setRecvBuffer(this.responseText);
                that.processInputPacket_(this.responseText);
            }
        };
        xreq.open('POST', this.xhrUrl_);
        if (xreq.setRequestHeader) {
            xreq.setRequestHeader('Accept', 'application/x-thrift');
            xreq.setRequestHeader('Content-Type', 'application/x-thrift');
        }

        xreq.send(this.send_buf);
    }
};

knock.thrift.TKnockTransport.prototype.getXmlHttpRequestObject = function() {
    try {
        return new XMLHttpRequest();
    } catch (e1) {
    }
    try {
        return new ActiveXObject('Msxml2.XMLHTTP');
    } catch (e2) {
    }
    try {
        return new ActiveXObject('Microsoft.XMLHTTP');
    } catch (e3) {
    }

    throw "Your browser doesn't support XHR.";
};

knock.thrift.TKnockTransport.prototype.processInputPacket_ = function(inputPacket) {
    var memoryInputTransport = new Thrift.TMemoryInputTransport(inputPacket);
    var memoryInputProtocol = new Thrift.Protocol(memoryInputTransport);
    var ret = memoryInputProtocol.readMessageBegin();

    var mtype = ret.mtype;
    var fname = ret.fname;
    var rseqid = ret.rseqid;

    if (mtype == Thrift.MessageType.REPLY || mtype == Thrift.MessageType.EXCEPTION) {
        var callback = Thrift.AsyncManager.getInstance().pop(rseqid);
        if (typeof callback !== 'undefined') {
            callback(inputPacket);
        }
    } else if (mtype == Thrift.MessageType.CALL) {
        var argsStruct = this.fnameToArgs(fname);
        argsStruct.read(memoryInputProtocol);
        memoryInputProtocol.readMessageEnd();

        var handler = Thrift.AsyncManager.getInstance().getClientCallback(fname);
        if (typeof handler !== 'undefined') {
            var memoryOutputTransport = new Thrift.TMemoryOutputTransport();
            var memoryOutputProtocol = new Thrift.Protocol(memoryOutputTransport);

            var args = [];
            for (var key in argsStruct) {
                if (argsStruct.hasOwnProperty(key)) {
                    var property = argsStruct[key];
                    args.push(property);
                }
            }
            var handlerResult = handler.apply(this, args);

            var result = this.fnameToResult(fname, {success: handlerResult});
            memoryOutputProtocol.writeMessageBegin(fname, Thrift.MessageType.REPLY, rseqid);
            result.write(memoryOutputProtocol);
            memoryOutputProtocol.writeMessageEnd();
            var packet = memoryOutputTransport.getPayload();
            this.write(packet);
            this.flush();
        }
    }
};

knock.thrift.TKnockTransport.prototype.setWsUrl = function(wsUrl) {
    if (wsUrl != this.wsUrl_) {
        this.wsUrl_ = wsUrl;
        if (this.wsSocket_) {
            this.close();
        }
        this.open();
    }
};

knock.thrift.TKnockTransport.prototype.setXhrUrl = function(xhrUrl, opt_xhrOptions) {
    this.xhrUrl_ = xhrUrl;
};

knock.thrift.TKnockTransport.prototype.isOpen = function() {
    throw Error('Shouldn\'t be used in knock-web');
};
knock.thrift.TKnockTransport.prototype.setRecvBuffer = function(buf) {
    this.recv_buf = buf;
};

knock.thrift.TKnockTransport.prototype.getSendBuffer = function() {
    throw Error('Shouldn\'t be used in knock-web');
};

knock.thrift.TKnockTransport.prototype.wsReset_ = function (url) {
    this.wsUrl_ = url;             //Where to connect
    this.wsSocket_ = null;         //The web socket
};

knock.thrift.TKnockTransport.prototype.isWsOpen_ = function() {
    return this.wsSocket_ && this.wsSocket_.readyState == this.wsSocket_.OPEN;
};


knock.thrift.TKnockTransport.prototype.onWsClose_ = function (evt) {
    this.wsReset_(this.wsUrl_);
    this.createSocket_();
};

knock.thrift.TKnockTransport.prototype.onWsMessage_ = function (evt) {
    var inputPacket = evt.data;
    if (!(inputPacket instanceof Blob)) {
        this.setRecvBuffer(evt.data);
        this.processInputPacket_(inputPacket);
    }
};

knock.thrift.TKnockTransport.prototype.onWsError_ = function (evt) {
    console.log("Thrift WebSocket Error: " + evt.toString());
    this.wsSocket_.close();
};

knock.thrift.TKnockTransport.prototype.createSocket_ = function() {
    this.wsSocket_ = new WebSocket(this.wsUrl_);
    window.socketTeam = this.wsSocket_;
    this.wsSocket_.onmessage = this.onWsMessage_.bind(this);
    this.wsSocket_.onerror = this.onWsError_.bind(this);
    this.wsSocket_.onclose = this.onWsClose_.bind(this);
};
