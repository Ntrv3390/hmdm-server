angular
  .module("plugin-worktime", ["ngResource", "ui.bootstrap", "ui.router"])
  .config(function ($stateProvider) {
    try {
      $stateProvider.state("plugin-worktime", {
        url: "/plugin-worktime",
        templateUrl: "app/components/main/view/content.html",
        controller: "TabController",
        resolve: {
          openTab: function () {
            return "plugin-worktime";
          },
        },
      });
    } catch (e) {
      console.log("Error adding state plugin-worktime", e);
    }

    $stateProvider.state("plugin-worktime-policies", {
      url: "/plugin-worktime",
      templateUrl:
        "app/components/plugins/worktime/views/worktime_policies.html",
      controller: "WorkTimePoliciesController",
    });

    $stateProvider.state("plugin-worktime-users", {
      url: "/plugin-worktime/users",
      templateUrl: "app/components/plugins/worktime/views/worktime_users.html",
      controller: "WorkTimeUsersController",
    });
  })
  .factory("WorkTimePolicy", function ($resource) {
    return $resource(
      "/rest/plugins/worktime/private/policy",
      {},
      {
        get: { method: "GET" },
        save: { method: "POST" },
      }
    );
  })
  .factory("WorkTimeUser", function ($resource) {
    return $resource("/rest/plugins/worktime/private/users/:userId", { userId: '@userId' }, {
      list: {
        method: 'GET',
        isArray: true,
        transformResponse: function(data) {
          var response = angular.fromJson(data);
          if (response && response.data) {
            return response.data;
          }
          return response || [];
        }
      },
      get: { method: 'GET' },
      save: { method: 'POST' },
      remove: { method: 'DELETE' }
    });
  })
  .factory("WorkTimeApplications", function ($resource) {
    return $resource("/rest/private/applications/search/:value", { value: '@value' }, {
      getAll: { method: 'GET' }
    });
  })
  .controller(
    "WorkTimePoliciesController",
    function ($scope, $timeout, WorkTimePolicy, WorkTimeApplications, localization) {
      $scope.policy = null;
      $scope.error = null;
      $scope.success = null;
      $scope.applications = [];
      $scope.appsLoading = true;
      $scope.selectedAppsDuringWork = {};
      $scope.selectedAppsOutsideWork = {};
      $scope.duringWorkSearchText = '';
      $scope.outsideWorkSearchText = '';

      $scope.days = [
        { id: 1, label: "Mon" },
        { id: 2, label: "Tue" },
        { id: 4, label: "Wed" },
        { id: 8, label: "Thu" },
        { id: 16, label: "Fri" },
        { id: 32, label: "Sat" },
        { id: 64, label: "Sun" },
      ];

      $scope.toggleDay = function (dayMask) {
        if (($scope.policy.daysOfWeek & dayMask) === dayMask) {
          $scope.policy.daysOfWeek &= ~dayMask;
        } else {
          $scope.policy.daysOfWeek |= dayMask;
        }
      };

      $scope.hasDay = function (dayMask) {
        return ($scope.policy.daysOfWeek & dayMask) === dayMask;
      };

      // Load available applications
      $scope.loadApplications = function () {
        $scope.appsLoading = true;
        WorkTimeApplications.getAll(
          {},
          function (response) {
            if (response && response.status === 'OK' && response.data) {
              $scope.applications = response.data.filter(function(app) {
                return app.pkg; // Only include apps with package names
              }).sort(function(a, b) {
                var nameA = (a.name || a.pkg).toLowerCase();
                var nameB = (b.name || b.pkg).toLowerCase();
                return nameA.localeCompare(nameB);
              });
            }
            $scope.appsLoading = false;
          },
          function (error) {
            console.error('Failed to load applications:', error);
            $scope.error = 'Failed to load applications';
            $scope.appsLoading = false;
          }
        );
      };

      // Convert comma-separated string to selected apps object
      $scope.parseAppsString = function (appsString) {
        var selected = {};
        if (appsString) {
          if (appsString.trim() === '*') {
            selected['*'] = true;
          } else {
            var packages = appsString.split(',');
            packages.forEach(function (pkg) {
              var trimmed = pkg.trim();
              if (trimmed) {
                selected[trimmed] = true;
              }
            });
          }
        }
        return selected;
      };

      // Convert selected apps object to comma-separated string
      $scope.buildAppsString = function (selectedApps) {
        if (selectedApps['*']) {
          return '*';
        }
        var packages = [];
        for (var pkg in selectedApps) {
          if (selectedApps.hasOwnProperty(pkg) && selectedApps[pkg] && pkg !== '*') {
            packages.push(pkg);
          }
        }
        return packages.join(',');
      };

      // Toggle "All Apps" selection
      $scope.toggleAllAppsDuringWork = function () {
        if ($scope.selectedAppsDuringWork['*']) {
          // When "All Apps" is selected, clear individual selections
          $scope.selectedAppsDuringWork = { '*': true };
        }
      };

      $scope.toggleAllAppsOutsideWork = function () {
        if ($scope.selectedAppsOutsideWork['*']) {
          // When "All Apps" is selected, clear individual selections
          $scope.selectedAppsOutsideWork = { '*': true };
        }
      };

      // When individual app is selected, uncheck "All Apps"
      $scope.toggleIndividualAppDuringWork = function (pkg) {
        if ($scope.selectedAppsDuringWork['*']) {
          delete $scope.selectedAppsDuringWork['*'];
        }
      };

      $scope.toggleIndividualAppOutsideWork = function (pkg) {
        if ($scope.selectedAppsOutsideWork['*']) {
          delete $scope.selectedAppsOutsideWork['*'];
        }
      };

      // Get filtered apps for during work
      $scope.getFilteredAppsDuringWork = function() {
        if (!$scope.applications) return [];
        if (!$scope.duringWorkSearchText) return $scope.applications;
        var search = $scope.duringWorkSearchText.toLowerCase();
        return $scope.applications.filter(function(app) {
          var name = (app.name || '').toLowerCase();
          var pkg = (app.pkg || '').toLowerCase();
          return name.indexOf(search) !== -1 || pkg.indexOf(search) !== -1;
        });
      };

      // Get filtered apps for outside work
      $scope.getFilteredAppsOutsideWork = function() {
        if (!$scope.applications) return [];
        if (!$scope.outsideWorkSearchText) return $scope.applications;
        var search = $scope.outsideWorkSearchText.toLowerCase();
        return $scope.applications.filter(function(app) {
          var name = (app.name || '').toLowerCase();
          var pkg = (app.pkg || '').toLowerCase();
          return name.indexOf(search) !== -1 || pkg.indexOf(search) !== -1;
        });
      };

      // Count selected apps (excluding 'All' option)
      $scope.countSelected = function (selectedApps) {
        if (selectedApps['*']) return 'All';
        var count = 0;
        for (var pkg in selectedApps) {
          if (selectedApps.hasOwnProperty(pkg) && selectedApps[pkg] && pkg !== '*') {
            count++;
          }
        }
        return count > 0 ? count : 'None';
      };

      $scope.refresh = function () {
        WorkTimePolicy.get(
          function (response) {
            if (response && response.status === "OK") {
              $scope.policy = response.data || {
                startTime: "09:00",
                endTime: "18:00",
                daysOfWeek: 31,
                allowedAppsDuringWork: "",
                allowedAppsOutsideWork: "*",
                enabled: true,
              };
            } else {
              $scope.policy = {
                startTime: "09:00",
                endTime: "18:00",
                daysOfWeek: 31,
                allowedAppsDuringWork: "",
                allowedAppsOutsideWork: "*",
                enabled: true,
              };
            }
            // Parse the apps string into selected apps
            $scope.selectedAppsDuringWork = $scope.parseAppsString($scope.policy.allowedAppsDuringWork);
            $scope.selectedAppsOutsideWork = $scope.parseAppsString($scope.policy.allowedAppsOutsideWork);
          },
          function () {
            $scope.error = localization.localize("error.request.failure");
          }
        );
      };

      $scope.save = function () {
        $scope.error = null;
        $scope.success = null;

        // Convert selected apps to comma-separated strings
        var policyToSave = angular.copy($scope.policy);
        policyToSave.allowedAppsDuringWork = $scope.buildAppsString($scope.selectedAppsDuringWork);
        policyToSave.allowedAppsOutsideWork = $scope.buildAppsString($scope.selectedAppsOutsideWork);

        WorkTimePolicy.save(
          policyToSave,
          function (response) {
            if (response && response.status === "OK") {
              $scope.policy = response.data;
              $scope.selectedAppsDuringWork = $scope.parseAppsString($scope.policy.allowedAppsDuringWork);
              $scope.selectedAppsOutsideWork = $scope.parseAppsString($scope.policy.allowedAppsOutsideWork);
              
              // Show success message
              $scope.success = "Work Time Policy saved successfully!";
              
              // Auto-hide success message after 3 seconds
              $timeout(function() {
                $scope.success = null;
              }, 3000);
            } else if (response && response.message) {
              $scope.error = response.message;
            }
          },
          function () {
            $scope.error = localization.localize("error.request.failure");
          }
        );
      };

      // Initialize
      $scope.loadApplications();
      $scope.refresh();
    }
  )

  .controller("WorkTimeUsersController", function($scope, $uibModal, WorkTimePolicy, WorkTimeUser, localization, authService) {
    $scope.users = [];
    $scope.globalPolicy = { enabled: true, startTime: '09:00', endTime: '18:00' };
    $scope.error = null;
    $scope.canEdit = authService.isSuperAdmin() || authService.hasPermission('settings');
    $scope.editingUser = null;
    $scope.editingException = null;
    var modalInstance = null;

    var parseLocalDate = function(value) {
      if (!value) return null;
      var d = new Date(value);
      if (!isNaN(d.getTime())) return d;
      return null;
    };

    var combineDateTime = function(dateValue, timeValue) {
      if (!dateValue || !timeValue) return null;
      var d = new Date(dateValue);
      if (isNaN(d.getTime())) return null;
      var hours;
      var minutes;
      if (angular.isDate(timeValue)) {
        hours = timeValue.getHours();
        minutes = timeValue.getMinutes();
      } else if (typeof timeValue === 'string') {
        var parts = timeValue.split(':');
        hours = parseInt(parts[0], 10) || 0;
        minutes = parseInt(parts[1], 10) || 0;
      } else {
        return null;
      }
      d.setHours(hours, minutes, 0, 0);
      return d;
    };

    // Load global policy
    $scope.loadGlobalPolicy = function() {
      WorkTimePolicy.get(function(response) {
        if (response && response.status === 'OK' && response.data) {
          $scope.globalPolicy = response.data;
        }
        $scope.loadUsers();
      });
    };

    var buildExceptionFromRange = function(user) {
      if (!user || !user.startDateTime || !user.endDateTime) return null;
      var start = new Date(user.startDateTime);
      var end = new Date(user.endDateTime);
      if (isNaN(start.getTime()) || isNaN(end.getTime())) return null;
      return {
        dateFrom: start,
        dateTo: end,
        timeFrom: ("0" + start.getHours()).slice(-2) + ':' + ("0" + start.getMinutes()).slice(-2),
        timeTo: ("0" + end.getHours()).slice(-2) + ':' + ("0" + end.getMinutes()).slice(-2),
        active: (new Date() >= start && new Date() <= end)
      };
    };

    var hasScheduledException = function(user) {
      return user && user.startDateTime && user.endDateTime;
    };

    var isActiveException = function(user) {
      if (user.exceptions && user.exceptions.length > 0) {
        return user.exceptions.some(function(exc) { return exc.active; });
      }
      if (hasScheduledException(user)) {
        var start = new Date(user.startDateTime);
        var end = new Date(user.endDateTime);
        if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
          return new Date() >= start && new Date() <= end;
        }
      }
      return false;
    };

    // Load users list
    $scope.loadUsers = function() {
      WorkTimeUser.list(function(response) {
        $scope.users = angular.isArray(response) ? response : [];
        $scope.users.forEach(function(user) {
          if (!user.exceptions) {
            user.exceptions = [];
          }
          user.exceptions.forEach(function(exc) {
            exc.dateFrom = parseLocalDate(exc.dateFrom);
            exc.dateTo = parseLocalDate(exc.dateTo);
          });
          if (user.exceptions.length === 0 && user.enabled === false) {
            var fallback = buildExceptionFromRange(user);
            if (fallback) {
              user.exceptions.push(fallback);
            }
          }
          var hasActive = isActiveException(user);
          if (hasScheduledException(user)) {
            user.toggleOn = false;
          } else {
            user.toggleOn = !!$scope.globalPolicy.enabled && !hasActive;
          }
        });
      }, function(error) {
        $scope.error = 'Failed to load users: ' + (error && error.data ? error.data : 'Unknown error');
        console.error('Error loading users:', error);
      });
    };

    // Toggle user exception (turn off means create exception)
    $scope.toggleUserException = function(user) {
      if (!$scope.canEdit || !$scope.globalPolicy.enabled) return;

      if (user.toggleOn) {
        if (user.exceptions && user.exceptions.length > 0) {
          WorkTimeUser.remove({ userId: user.userId }, function() {
            $scope.loadUsers();
          });
        }
        return;
      }

      $scope.editingUser = user;
      var existing = (user.exceptions && user.exceptions.length > 0) ? user.exceptions[0] : null;
      $scope.editingException = existing ? angular.copy(existing) : {
        dateFrom: new Date(),
        dateTo: new Date(),
        timeFrom: '09:00',
        timeTo: '18:00'
      };
      $scope.openExceptionModal();
    };

    // Edit existing exception
    $scope.editException = function(user) {
      if (!$scope.canEdit || !$scope.globalPolicy.enabled) return;
      $scope.editingUser = user;
      $scope.editingException = angular.copy(user.exceptions[0]) || {};
      $scope.openExceptionModal();
    };

    // Save exception
    $scope.saveException = function() {
      if (!$scope.editingUser || !$scope.editingException) return;

      var startDateTime = combineDateTime($scope.editingException.dateFrom, $scope.editingException.timeFrom);
      var endDateTime = combineDateTime($scope.editingException.dateTo, $scope.editingException.timeTo);
      if (!startDateTime || !endDateTime) {
        $scope.error = 'Start and end date/time are required';
        return;
      }
      if (endDateTime < startDateTime) {
        $scope.error = 'End time must be after start time';
        return;
      }
      if (endDateTime < new Date()) {
        $scope.error = 'End time must be in the future';
        return;
      }

      var override = {
        userId: $scope.editingUser.userId,
        enabled: false,
        startDateTime: startDateTime.toISOString(),
        endDateTime: endDateTime.toISOString()
      };

      WorkTimeUser.save({ userId: $scope.editingUser.userId }, override, function(response) {
        if (response && response.status === 'OK') {
          $scope.closeExceptionModal();
          $scope.loadUsers();
        } else {
          $scope.error = response.message || 'Failed to save exception';
        }
      }, function() {
        $scope.error = localization.localize('error.request.failure');
      });
    };

    $scope.cancelException = function() {
      if ($scope.editingUser) {
        $scope.editingUser.toggleOn = true;
      }
      $scope.editingUser = null;
      $scope.editingException = null;
      $scope.closeExceptionModal();
    };

    $scope.openExceptionModal = function() {
      if (modalInstance) return;
      modalInstance = $uibModal.open({
        templateUrl: 'worktimeExceptionModalTemplate.html',
        scope: $scope,
        backdrop: 'static',
        keyboard: true
      });
      modalInstance.result.finally(function() {
        modalInstance = null;
      });
    };

    $scope.closeExceptionModal = function() {
      if (modalInstance) {
        modalInstance.close();
        modalInstance = null;
      }
    };

    // Remove exception
    $scope.removeException = function(user, exception) {
      if (!confirm('Delete this exception?')) return;
      WorkTimeUser.remove({ userId: user.userId }, function() {
        $scope.loadUsers();
      });
    };

    $scope.loadGlobalPolicy();
  })
  .run(function (localization) {
    localization.loadPluginResourceBundles("worktime");
  });
