// Generated by CoffeeScript 1.10.0
(function() {
  var formLogicExt,
    hasProp = {}.hasOwnProperty;

  formLogicExt = function($, WebOmi) {
    var my;
    my = WebOmi.formLogic = {};

    my.authServer = "http://localhost:8088";
    my.setRequest = function(xml) {
      var mirror;
      mirror = WebOmi.consts.requestCodeMirror;
      if (xml == null) {
        mirror.setValue("");
      } else if (typeof xml === "string") {
        mirror.setValue(xml);
      } else {
        mirror.setValue(new XMLSerializer().serializeToString(xml));
      }
      return mirror.autoFormatAll();
    };
    my.getRequest = function() {
      var str;
      str = WebOmi.consts.requestCodeMirror.getValue();
      return WebOmi.omi.parseXml(str);
    };
    my.modifyRequest = function(callback) {
      var req;
      req = my.getRequest();
      callback();
      return WebOmi.requests.generate();
    };
    my.getRequestOdf = function() {
      var str;
      WebOmi.error("getRequestOdf is deprecated");
      str = WebOmi.consts.requestCodeMirror.getValue();
      return o.evaluateXPath(str, '//odf:Objects')[0];
    };
    my.clearResponse = function() {
      var mirror;
      mirror = WebOmi.consts.responseCodeMirror;
      mirror.setValue("");
      return WebOmi.consts.responseDiv.slideUp();
    };
    my.setResponse = function(xml) {
      var mirror;
      mirror = WebOmi.consts.responseCodeMirror;
      if (typeof xml === "string") {
        mirror.setValue(xml);
      } else {
        mirror.setValue(new XMLSerializer().serializeToString(xml));
      }
      mirror.autoFormatAll();
      WebOmi.consts.responseDiv.slideDown({
        complete: function() {
          return mirror.refresh();
        }
      });
      return mirror.refresh();
    };
    my.send = function(callback) {
      var consts, request, server;
      consts = WebOmi.consts;
      my.clearResponse();
      server = consts.serverUrl.val();
      request = consts.requestCodeMirror.getValue();
      consts.progressBar.css("width", "95%");
      return $.ajax({
        type: "POST",
        url: server,
        data: request,
        contentType: "text/xml",
        processData: false,
        dataType: "text",
        error: function(response) {
          consts.progressBar.css("width", "100%");
          my.setResponse(response.responseText);
          consts.progressBar.css("width", "0%");
          consts.progressBar.hide();
          return window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        },
        success: function(response) {
          consts.progressBar.css("width", "100%");
          my.setResponse(response);
          consts.progressBar.css("width", "0%");
          consts.progressBar.hide();
          window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
          if ((callback != null)) {
            return callback(response);
          }
        }
      });
    };

    my.readGroups = function(callback) {
      var consts, server;
      consts = WebOmi.consts;
      server = my.authServer + "/PermissionService?readGroups=true";

      return $.ajax({
        type: "GET",
        url: server,
        contentType: "text/xml",
        processData: false,
        dataType: "text",
        error: function(response) {
          console.log(response);
          return window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        },
        success: function(response) {
          console.log("Read groups successfully! Response:"+response);

          // populate table
          var json_response = $.parseJSON(response);
          WebOmi.formLogic.readedGroups = json_response;
          $(function() {
              $.each(json_response['groups'], function(i, item) {
                  $('<option value="'+ item.id +'">').text(item.name).appendTo('#groupsSelect');
              });

              $("#groupsSelect").trigger("chosen:updated");
          });

          window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
          if ((callback != null)) {
            return callback(response);
          }
        }
      });
    };

    my.deleteGroup = function() {
      var consts, server;
      consts = WebOmi.consts;

      var groupID = $("#groupsSelect").val();
      server = my.authServer + "/PermissionService?removeGroups=true&groupID=" + groupID;

      return $.ajax({
        type: "GET",
        url: server,
        contentType: "text/xml",
        processData: false,
        dataType: "text",
        error: function(response) {
          console.log(response);
          return window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        },
        success: function(response) {
          console.log("Delete group successfully! Response:"+response);

          $('#groupsSelect option[value="'+groupID+'"]').remove();

          location.reload();
          window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        }
      });
    };

    my.updateGroups = function(newGroupItem) {
      var consts, request, server;
      consts = WebOmi.consts;
      server = my.authServer + "/PermissionService?writeGroups=true";
      consts.progressBar.css("width", "95%");
      request = JSON.stringify(newGroupItem);
      console.log(request);
      return $.ajax({
        type: "POST",
        url: server,
        data: request,
        contentType: "application/json",
        processData: false,
        dataType: "text",
        error: function(response) {
          consts.progressBar.css("width", "100%");
          my.setResponse(response.responseText);
          consts.progressBar.css("width", "0%");
          consts.progressBar.hide();
          return window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        },
        success: function(response) {
          consts.progressBar.css("width", "100%");
          my.setResponse(response);
          consts.progressBar.css("width", "0%");
          consts.progressBar.hide();

          console.log(response);
          var json_response = $.parseJSON(response);

          if (json_response['groupID'] != null) {
            newGroupItem['id'] = json_response['groupID'];
            WebOmi.formLogic.readedGroups['groups'].push(newGroupItem);
            $('<option value="'+ newGroupItem['id'] +'">').text(newGroupItem.name).appendTo('#groupsSelect');
          } else {
            $(function() {
                $.each(WebOmi.formLogic.readedGroups['groups'], function(i, item) {
                  if (item["id"] == newGroupItem["id"]) {
                    item["name"] = newGroupItem["name"];
                    item["values"] = newGroupItem["values"];

                    $('#groupsSelect option[value="'+item["id"]+'"]').text(item["name"]);
                  }
                });
            });
          }
          $("#groupsSelect").trigger("chosen:updated");
          if (newGroupItem['id'] != null) {
            $("#groupsSelect").val(newGroupItem['id']).trigger("change");
            $("#groupsSelect").trigger("chosen:updated");
          }

          window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        }
      });
    }

    my.readRules = function(groupID, callback) {
      var consts, server;
      consts = WebOmi.consts;
      server = my.authServer + "/PermissionService?readRules=true&groupID="+groupID;
      tree = WebOmi.consts.odfTree;

      setAttribute = function (obj, key, value) {

        console.log(obj);
        var i = 1,
            attrs = key.split('/'),
            max = attrs.length;

        for (; i < max; i++) {
            attr = attrs[i];

            var children = obj["children"];

            for (var j=0; j<children.length; ++j) {
              if (children[j].id == key) {
                console.log("Writing value!");
                children[j].text = value;
                return;
              }

              // children[j].text.replace("[R]","").replace("[W]","");

              if (key.indexOf(children[j].id) > -1) {
                obj = children[j];
                break;
              }
            }

        }
      }

      return $.ajax({
        type: "GET",
        url: server,
        contentType: "text/xml",
        processData: false,
        dataType: "text",
        error: function(response) {
          console.log(response);
          return window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        },
        success: function(response) {
          console.log("Read rules successfully! Response:"+response);

          var json_response = $.parseJSON(response);
          WebOmi.formLogic.readedRules = json_response;

          $(function() {
              if (json_response['rules'] != null) {

                var dataString = JSON.stringify(tree.settings.core.data);

                escapeRegExp = function (str) {
                    return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
                }

                dataString = dataString.replace(new RegExp(escapeRegExp("[R]"), 'g'), '')
                                       .replace(new RegExp(escapeRegExp("[W]"), 'g'), '');
                tree.settings.core.data =JSON.parse(dataString);

                $.each(json_response['rules'], function(i, item) {

                    var objectName = item.hid.substring(item.hid.lastIndexOf("/")+1);
                    if (item.writePermissions == "1") {
                        objectName += "[W]";
                    } else {
                        objectName += "[R]";
                    }

                    setAttribute(tree.settings.core.data[0], item.hid, objectName);

                });


                tree.refresh();
              }
          });

          window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
          if ((callback != null)) {
            return callback(response);
          }
        }
      });
    };

    my.readUsers = function(callback) {
      var consts, server;
      consts = WebOmi.consts;
      server = my.authServer + "/PermissionService?readUsers=true";

      return $.ajax({
        type: "GET",
        url: server,
        contentType: "text/xml",
        processData: false,
        dataType: "text",
        error: function(response) {
          console.log(response);
          return window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        },
        success: function(response) {
          console.log("Read users successfully! Response:"+response);

          // populate table
          var json_response = $.parseJSON(response);
          WebOmi.formLogic.readedUsers = json_response;
          $(function() {
              if (json_response['users'] != null) {
                $.each(json_response['users'], function(i, item) {
                    $('<option value="'+ item.id +'">').text(item.username + " [" + item.email +"]")
                      .appendTo('#addUsersSelect');
                });

                $("#addUsersSelect").trigger("chosen:updated");
              }
          });

          window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
          if ((callback != null)) {
            return callback(response);
          }
        }
      });
    };

    my.sendPerm = function(callback) {
      var consts, request, server;
      consts = WebOmi.consts;
      my.clearResponse();
      var tree = WebOmi.consts.odfTree;
      var treeDom = WebOmi.consts.odfTreeDom;
      var groupID = $("#groupsSelect").chosen().val();
      server = my.authServer + "/PermissionService?writeRules=true&groupID="+groupID;
      // TODO: make server URL changeable
      //consts.serverUrl.val();
      request = consts.requestCodeMirror.getValue();
      consts.progressBar.css("width", "95%");
      return $.ajax({
        type: "POST",
        url: server,
        data: request,
        contentType: "text/xml",
        processData: false,
        dataType: "text",
        error: function(response) {
          consts.progressBar.css("width", "100%");
          my.setResponse(response.responseText);
          consts.progressBar.css("width", "0%");
          consts.progressBar.hide();
          return window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
        },
        success: function(response) {
          console.log("User Permissions sent! Response:"+response);

          $("#nodetree").jstree('close_all');
          tree.deselect_all();

          consts.progressBar.css("width", "100%");
          my.setResponse(response);
          consts.progressBar.css("width", "0%");
          consts.progressBar.hide();
          window.setTimeout((function() {
            return consts.progressBar.show();
          }), 2000);
          if ((callback != null)) {
            return callback(response);
          }
        }
      });
    };
    my.buildOdfTree = function(objectsNode) {
      var evaluateXPath, genData, objChildren, tree, treeData;
      tree = WebOmi.consts.odfTree;
      evaluateXPath = WebOmi.omi.evaluateXPath;
      objChildren = function(xmlNode) {
        return evaluateXPath(xmlNode, './odf:InfoItem | ./odf:Object');
      };
      genData = function(xmlNode, parentPath) {
        var child, name, path;
        switch (xmlNode.nodeName) {
          case "Objects":
            name = xmlNode.nodeName;
            return {
              id: idesc(name),
              text: name,
              state: {
                opened: true
              },
              type: "objects",
              children: (function() {
                var i, len, ref, results;
                ref = objChildren(xmlNode);
                results = [];
                for (i = 0, len = ref.length; i < len; i++) {
                  child = ref[i];
                  results.push(genData(child, name));
                }
                return results;
              })()
            };
          case "Object":
            name = WebOmi.omi.getOdfId(xmlNode);
            path = parentPath + "/" + name;
            return {
              id: idesc(path),
              text: name,
              type: "object",
              children: (function() {
                var i, len, ref, results;
                ref = objChildren(xmlNode);
                results = [];
                for (i = 0, len = ref.length; i < len; i++) {
                  child = ref[i];
                  results.push(genData(child, path));
                }
                return results;
              })()
            };
          case "InfoItem":
            name = WebOmi.omi.getOdfId(xmlNode);
            path = parentPath + "/" + name;
            return {
              id: idesc(path),
              text: name,
              type: "infoitem",
              children: [
                genData({
                  nodeName: "MetaData"
                }, path)
              ]
            };
          case "MetaData":
            path = parentPath + "/MetaData";
            return {
              id: idesc(path),
              text: "MetaData",
              type: "metadata",
              children: []
            };
        }
      };
      treeData = genData(objectsNode);
      tree.settings.core.data = [treeData];
      console.log(JSON.stringify(treeData, null, 2));
      return tree.refresh();
    };
    my.buildOdfTreeStr = function(responseString) {
      var objectsArr, omi, parsed;
      omi = WebOmi.omi;
      parsed = omi.parseXml(responseString);
      objectsArr = omi.evaluateXPath(parsed, "//odf:Objects");
      if (objectsArr.length !== 1) {
        return alert("failed to get single Objects odf root");
      } else {
        return my.buildOdfTree(objectsArr[0]);
      }
    };
    return WebOmi;
  };

  window.WebOmi = formLogicExt($, window.WebOmi || {});

  (function(consts, requests, formLogic) {
    return consts.afterJquery(function() {
      var controls, inputVar, makeRequestUpdater, ref;

      formLogic.readUsers();
      formLogic.readGroups();
      requests.readAll(true);

      consts.saveBtn.on('click', function() {
        return formLogic.sendPerm();
      });
      consts.addGroupBtn.on('click', function() {
        return WebOmi.consts.groupItemDialog.modal("show");
      });
      consts.deleteGroupBtn.on('click', function() {
        var result = confirm("Do you want to delete the group?");
        if (result) {
            return formLogic.deleteGroup();
        }
      });
      consts.resetAllBtn.on('click', function() {
        var child, closetime, i, len, ref;
        requests.forceLoadParams(requests.defaults.empty());
        closetime = 1500;
        ref = consts.odfTree.get_children_dom('Objects');
        for (i = 0, len = ref.length; i < len; i++) {
          child = ref[i];
          consts.odfTree.close_all(child, closetime);
        }
        return formLogic.clearResponse();
      });
      consts.ui.odf.ref.on("changed.jstree", function(event, data) {
        var odfTreePath;
        switch (data.action) {
          case "select_node":
            odfTreePath = data.node.id;
            // console.log(odfTreePath);
            return formLogic.modifyRequest(function() {
              return requests.params.odf.add(odfTreePath);
            });
          case "deselect_node":
            odfTreePath = data.node.id;
            formLogic.modifyRequest(function() {
              return requests.params.odf.remove(odfTreePath);
            });
            return $(jqesc(odfTreePath)).children(".jstree-children").find(".jstree-node").each(function(_, node) {
              return consts.odfTree.deselect_node(node);
            });
        }
      });

      makeRequestUpdater = function(input) {
        return function(val) {
          return formLogic.modifyRequest(function() {
            return requests.params[input].update(val);
          });
        };
      };
      ref = consts.ui;
      for (inputVar in ref) {
        if (!hasProp.call(ref, inputVar)) continue;
        controls = ref[inputVar];
        if (controls.bindTo != null) {
          controls.bindTo(makeRequestUpdater(inputVar));
        }
      }
      return null;
    });
  })(window.WebOmi.consts, window.WebOmi.requests, window.WebOmi.formLogic);

  $(function() {
    return $('.optional-parameters > a').on('click', function() {
      var glyph;
      glyph = $(this).find('span.glyphicon');
      if (glyph.hasClass('glyphicon-menu-right')) {
        glyph.removeClass('glyphicon-menu-right');
        return glyph.addClass('glyphicon-menu-down');
      } else {
        glyph.removeClass('glyphicon-menu-down');
        return glyph.addClass('glyphicon-menu-right');
      }
    });
  });

}).call(this);