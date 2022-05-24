const modalOpen = (text, type, button, buttonURL) => {
  const modal = document.getElementsByClassName("modal")[0];

  modal.firstElementChild.innerHTML = text;
  modal.classList.toggle(type);

  if (button && buttonURL) {
    const buttonElement = document.createElement("a");
    buttonElement.classList = "modalbutton aux";
    buttonElement.setAttribute(
      "onclick",
      "setModalButton('loading'," + type + ");" + buttonURL + ";return false"
    );
    buttonElement.text = button;

    modal.insertBefore(
      buttonElement,
      modal.lastElementChild.previousElementSibling
    );
  }
};

const modalClose = () => {
  const modal = document.getElementsByClassName("modal")[0];

  modal.firstElementChild.innerHTML = "";
  modal.classList = "modal";

  if (document.getElementsByClassName("modalbutton aux").length != 0) {
    document.getElementsByClassName("modalbutton aux")[0].remove();
  }
};

function request(method, url, data) {
  const apiURL = "http://localhost:4567/api";
  if (method != "GET") {
    return fetch(apiURL + url, {
      method: method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }).then((response) => response.json());
  } else {
    return fetch(apiURL + url, {
      method: method,
    })
      .then((response) => response.json())
      .catch((error) => modalOpen(error, "error"));
  }
}

const parseUpdateState = (updateState, id) => {
  switch (updateState) {
    case 0:
      return '</p><a class="passive">' + "File is in sync" + "</a>";
    case 1:
      return (
        "</p><a onclick='syncFiles(" +
        id +
        ", 1)'>" +
        "Arvuti -> Github" +
        "</a>"
      );
    case 2:
      return (
        "</p><a onclick='syncFiles(" +
        id +
        ", 2)'>" +
        "Github -> Arvuti" +
        "</a>"
      );
    case 3:
      return (
        "</p><div class='splitbutton'><a onclick='syncFiles(" +
        id +
        ", 1)'>" +
        "Arvuti -> Github" +
        "</a>" +
        "<a onclick='syncFiles(" +
        id +
        ", 2)'>" +
        "Github -> Arvuti" +
        "</a></div>"
      );
  }
};

const getAuth = () => {
  return request("GET", "/auth", "").then((a) => {
    if ((a == true) | (a == false)) {
      return a;
    }
    modalOpen(a, "error");
    return false;
  });
};

const submitAuth = (element) => {
  setModalButton("loading", "auth");
  const data = {
    user: element.elements["username"].value,
    pass: element.elements["password"].value,
  };

  request("POST", "/auth", data).then((response) => {
    if (response == true) {
      modalClose();
      clearFiles();
      getFiles();
    } else {
      setModalButton("failed", "auth");
    }
  });
};

const authDialog = () => {
  getAuth().then((response) => {
    if (!response) {
      modalOpen(
        "<b>Please provide your GitHub credentials.</b> <br/> <br/> Access tokens are preferred <a>https://github.com/settings/tokens</a>",
        "login"
      );
    }
  });
};

const setModalButton = (option, type) => {
  const wrapper = document.getElementsByClassName(
    type == "auth" ? "form-box auth" : "modal"
  )[0];

  const button = document.getElementsByClassName(
    type == "auth" ? "filebutton auth" : "modalbutton aux"
  )[0];

  wrapper.classList.remove("loading");
  wrapper.classList.remove("failed");

  switch (option) {
    case "loading":
      wrapper.classList.add("loading");
      break;
    case "failed":
      wrapper.classList.add("failed");
      button.textContent = "Try again";
      break;
  }
};

const getFiles = () => {
  request("GET", "/files", "").then((a) => {
    a.map((b) => {
      addFile(b["nimi"], b["path"], b["muudatus"]);
    });
  });
};

const clearFiles = () => {
  const fileTable = document.getElementsByClassName("filetable")[0];
  while (fileTable.childElementCount > 2) {
    fileTable.removeChild(fileTable.firstElementChild.nextElementSibling);
  }

  fileTable.lastElementChild.firstElementChild.nextElementSibling.innerText = 0;
};

const syncFiles = (id, locationID) => {
  const location = locationID == 1 ? "git" : "local";
  const data = { id: String(id), loc: location };

  document.body.style.cursor = "wait";
  request("POST", "/sync", data).then((a) => {
    if (a != "OK") {
      modalOpen(a, "error");
    } else {
      clearFiles();
      getFiles();
      document.body.style.cursor = "auto";
    }
  });
};

const removeFile = (id) => {
  const row = document.getElementById(id);
  const pathAndName = row.childNodes[3].innerText + row.childNodes[2].innerText;
  const data = { path: pathAndName };

  request("DELETE", "/files", data).then((a) => {
    if (a != "OK") {
      modalOpen(a, "error");
    } else {
      clearFiles();
      getFiles();
      modalClose();
    }
  });
};

const removeDialog = (id) => {
  const row = document.getElementById(id);
  const pathAndName = row.childNodes[3].innerText + row.childNodes[2].innerText;

  modalOpen(
    "You're about to stop tracking <b>" + pathAndName + "</b> on this device.",
    "confirm",
    "Continue",
    "removeFile(" + id + ")"
  );
};

const addDialog = (element) => {
  const path = element.elements["path"].value;
  const name = element.elements["name"].value;

  modalOpen(
    "You're about to add tracking to <b>" +
      path +
      name +
      "</b> on this device.",
    "confirm",
    "Continue",
    "addFromInput()"
  );
};

const addFromInput = () => {
  const form = document.getElementsByClassName("fileadd")[0];
  const pathAndName = form.elements["path"].value + form.elements["name"].value;
  const data = { path: pathAndName };

  request("POST", "/files", data).then((a) => {
    if (a != "OK") {
      modalClose();
      modalOpen(a, "error");
    } else {
      clearFiles();
      getFiles();
      form.elements["path"].value = "";
      form.elements["name"].value = "";
      modalClose();
    }
  });
};

const addFile = (name, path, updateState) => {
  const table = document.getElementsByClassName("filetable")[0];
  const form = document.getElementsByClassName("form-box fileadd")[0];
  const nextId = form.firstElementChild.nextElementSibling.innerText;

  const action = parseUpdateState(updateState, nextId);
  const newRow = document.createElement("span");
  newRow.id = nextId;

  newRow.innerHTML =
    "<a class='filebutton remove' onclick='removeDialog(" +
    nextId +
    ")' title='Stop tracking this file'>-</a>" +
    '<p class="id">' +
    nextId +
    '</p><p class="name">' +
    name +
    '</p><p class="path">' +
    path +
    action;

  table.lastElementChild.before(newRow);
  form.firstElementChild.nextElementSibling.innerText = Number(nextId) + 1;
};

document.onkeydown = function (e) {
  if (e.key === "Escape") {
    modalClose();
  }
};

authDialog();
getFiles();
