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
      url: "/plugin/worktime/policies",
      templateUrl:
        "app/components/plugins/worktime/views/worktime_policies.html",
      controller: "WorkTimePoliciesController",
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
  .controller(
    "WorkTimePoliciesController",
    function ($scope, WorkTimePolicy, localization) {
      $scope.policy = null;
      $scope.error = null;

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

      $scope.refresh = function () {
        WorkTimePolicy.get(
          function (response) {
            if (response && response.status === "OK") {
              $scope.policy =
                response.data || {
                  startTime: "09:00",
                  endTime: "18:00",
                  daysOfWeek: 31,
                  allowedAppsDuringWork: "",
                  allowedAppsOutsideWork: "*",
                };
            } else {
              $scope.policy = {
                startTime: "09:00",
                endTime: "18:00",
                daysOfWeek: 31,
                allowedAppsDuringWork: "",
                allowedAppsOutsideWork: "*",
              };
            }
          },
          function () {
            $scope.error = localization.localize("error.request.failure");
          }
        );
      };

      $scope.save = function () {
        $scope.error = null;

        WorkTimePolicy.save(
          $scope.policy,
          function (response) {
            if (response && response.status === "OK") {
              $scope.policy = response.data;
            } else if (response && response.message) {
              $scope.error = response.message;
            }
          },
          function () {
            $scope.error = localization.localize("error.request.failure");
          }
        );
      };

      $scope.refresh();
    }
  )
  .run(function (localization) {
    localization.loadPluginResourceBundles("worktime");
  });
