import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Table } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import * as Constants from "../../common/Constants";
import TaskContributorService from "../../service/TaskContributorService";
import { useToast } from "../../components/Toast";
import { useTableSort } from "../../hooks/useTableSort";
import SortableTableHeader from "../../components/SortableTableHeader";

function MyTaskAssignments() {
    const { t } = useTranslation();
    const { showError } = useToast();
    const [taskContributors, setTaskContributors] = useState([]);
    const [accountNameFilter, setAccountNameFilter] = useState("");
    const [taskNameFilter, setTaskNameFilter] = useState("");
    const [statusFilter, setStatusFilter] = useState(Constants.ACTIVE_STATUS);
    const { sortedData, requestSort, getSortIcon } = useTableSort(taskContributors, "task.account.name");

    const loadAssignments = useCallback(() => {
        const taskContributorService = new TaskContributorService();
        taskContributorService.getMyTaskContributors()
            .then((response) => {
                setTaskContributors(response.data || []);
            })
            .catch((error) => {
                showError(t("taskContributors.messages.loadTaskContributorsFailed") + ": " + (error.response?.data?.error || error.message));
            });
    }, [showError, t]);

    useEffect(() => {
        loadAssignments();
    }, [loadAssignments]);

    const handleActivationChange = useCallback((taskContributor, nextStatus) => {
        const taskContributorService = new TaskContributorService();
        const apiCall = nextStatus === Constants.ACTIVE_STATUS
            ? taskContributorService.selfAssign(taskContributor.task.id)
            : taskContributorService.selfUnassign(taskContributor.task.id);

        apiCall
            .then(() => {
                setTaskContributors((prev) => prev.map((item) => (
                    item.task.id === taskContributor.task.id
                        ? { ...item, activationStatus: nextStatus }
                        : item
                )));
            })
            .catch((error) => {
                showError(t("taskContributors.messages.updateFailed") + ": " + (error.response?.data?.error || error.message));
            });
    }, [showError, t]);

    const filteredTaskContributors = useMemo(() => (sortedData || []).filter((taskContributor) => (
        taskContributor.activationStatus === statusFilter
        && taskContributor.task.account.name.toLowerCase().startsWith(accountNameFilter.toLowerCase())
        && taskContributor.task.name.toLowerCase().startsWith(taskNameFilter.toLowerCase())
    )), [sortedData, statusFilter, accountNameFilter, taskNameFilter]);

    return (
        <div className="container-fluid ml-2 mr-2">
            <div className="card">
                <div className="card-header">
                    <h4>{t("taskContributors.myTasksTitle")}</h4>
                </div>
                <div className="card-body">
                    <div className="row mb-3">
                        <div className="col-sm-2">
                            <input
                                className="form-control input-sm"
                                type="text"
                                placeholder={t("taskContributors.placeholders.filterByAccount")}
                                value={accountNameFilter}
                                onChange={(event) => setAccountNameFilter(event.target.value)}
                            />
                        </div>
                        <div className="col-sm-2">
                            <input
                                className="form-control input-sm"
                                type="text"
                                placeholder={t("taskContributors.placeholders.filterByTask")}
                                value={taskNameFilter}
                                onChange={(event) => setTaskNameFilter(event.target.value)}
                            />
                        </div>
                        <div className="col-sm-2">
                            <select
                                className="form-control input-sm"
                                value={statusFilter}
                                onChange={(event) => setStatusFilter(event.target.value)}
                            >
                                <option value={Constants.ACTIVE_STATUS}>{t("common.status.active")}</option>
                                <option value={Constants.INACTIVE_STATUS}>{t("common.status.inactive")}</option>
                            </select>
                        </div>
                    </div>

                    <Table striped bordered hover responsive>
                        <thead className="bg-success">
                            <tr>
                                <SortableTableHeader
                                    field="task.account.name"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t("taskContributors.headers.account")}
                                </SortableTableHeader>
                                <SortableTableHeader
                                    field="task.name"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t("taskContributors.headers.task")}
                                </SortableTableHeader>
                                <SortableTableHeader
                                    field="activationStatus"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t("taskContributors.headers.status")}
                                </SortableTableHeader>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredTaskContributors.map((taskContributor) => (
                                <tr key={`${taskContributor.task.account.id}_${taskContributor.task.id}`}>
                                    <td>{taskContributor.task.account.name}</td>
                                    <td>{taskContributor.task.name}</td>
                                    <td>
                                        <select
                                            className="form-control input-sm"
                                            value={taskContributor.activationStatus}
                                            onChange={(event) => handleActivationChange(taskContributor, event.target.value)}
                                        >
                                            <option value={Constants.ACTIVE_STATUS}>{t("common.status.active")}</option>
                                            <option value={Constants.INACTIVE_STATUS}>{t("common.status.inactive")}</option>
                                        </select>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </Table>
                </div>
            </div>
        </div>
    );
}

export default MyTaskAssignments;
